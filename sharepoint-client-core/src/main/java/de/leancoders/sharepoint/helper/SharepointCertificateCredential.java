package de.leancoders.sharepoint.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.leancoders.sharepoint.model.SharepointCertificateConfig;
import de.leancoders.sharepoint.model.SharepointClientAssertionClaims;
import de.leancoders.sharepoint.model.SharepointClientAssertionHeader;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HexFormat;

/**
 * Builds the signed JWT that replaces the client secret when authenticating against SharePoint Online.
 *
 * <p>SharePoint refuses app-only tokens whose {@code appidacr} claim is {@code 1} (client secret) and only accepts
 * {@code 2} (certificate based client assertion), so the token request has to present a JWT signed with the private
 * key belonging to a certificate uploaded to the app registration.
 *
 * <p>Only two things are actually needed: an RSA private key to sign with, and the certificate's SHA-1 thumbprint
 * for the {@code x5t} header so Entra knows which of the app's registered certificates to verify against. A
 * keystore is merely one way to supply them - see the {@code from*} / {@code of} factories.
 *
 * @see <a href="https://learn.microsoft.com/en-us/entra/identity-platform/certificate-credentials">Microsoft identity platform application authentication certificate credentials</a>
 */
public final class SharepointCertificateCredential {

    public static final String CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    /**
     * Entra rejects assertions valid for much longer; ten minutes is the documented maximum.
     */
    private static final Duration LIFETIME = Duration.ofMinutes(10);

    private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();

    @Nonnull
    private final PrivateKey privateKey;
    /**
     * Already base64url encoded - this is what goes into the JWT header verbatim.
     */
    @Nonnull
    private final String x5t;
    @Nonnull
    private final ObjectMapper objectMapper = ObjectMapperFactory.createDefaultObjectMapper();

    private SharepointCertificateCredential(@Nonnull final PrivateKey privateKey, @Nonnull final String x5t) {
        this.privateKey = privateKey;
        this.x5t = x5t;
    }

    // ---------------------------------------------------------------------------------------------------------
    // factories
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Loads key and certificate from a PKCS#12 keystore.
     */
    @Nonnull
    public static SharepointCertificateCredential fromKeyStore(@NonNull final SharepointCertificateConfig config) {
        try {
            final char[] password = config.getKeyStorePassword().toCharArray();
            final KeyStore keyStore = KeyStore.getInstance("PKCS12");

            try (InputStream in = Files.newInputStream(Paths.get(config.getKeyStorePath()))) {
                keyStore.load(in, password);
            }

            final String alias = resolveAlias(keyStore, config.getKeyAlias());

            final PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
            final X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

            if (privateKey == null || certificate == null) {
                throw new IllegalStateException("keystore %s has no key entry for alias '%s'".formatted(config.getKeyStorePath(), alias));
            }

            return of(privateKey, certificate);
        }
        catch (final RuntimeException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new IllegalStateException("could not load keystore %s".formatted(config.getKeyStorePath()), e);
        }
    }

    /**
     * Builds an assertion from PEM content, e.g. read from a secret manager or an environment variable.
     *
     * @param privateKeyPem  an unencrypted <b>PKCS#8</b> key - the block starting {@code -----BEGIN PRIVATE KEY-----}
     * @param certificatePem the certificate - {@code -----BEGIN CERTIFICATE-----}
     */
    @Nonnull
    public static SharepointCertificateCredential fromPem(@NonNull final String privateKeyPem,
                                                          @NonNull final String certificatePem) {
        return of(privateKeyFromPem(privateKeyPem), certificateFromPem(certificatePem));
    }

    /**
     * Same as {@link #fromPem(String, String)} but reading both blocks from files.
     */
    @Nonnull
    public static SharepointCertificateCredential fromPem(@NonNull final Path privateKeyFile,
                                                          @NonNull final Path certificateFile) {
        try {
            return fromPem(Files.readString(privateKeyFile), Files.readString(certificateFile));
        }
        catch (final RuntimeException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new IllegalStateException(
                "could not read pem files %s / %s".formatted(privateKeyFile, certificateFile), e);
        }
    }

    /**
     * Bring your own key material, however it was obtained.
     */
    @Nonnull
    public static SharepointCertificateCredential of(@NonNull final PrivateKey privateKey,
                                                     @NonNull final X509Certificate certificate) {
        try {
            return new SharepointCertificateCredential(
                privateKey,
                encode(MessageDigest.getInstance("SHA-1").digest(certificate.getEncoded())));
        }
        catch (final Exception e) {
            throw new IllegalStateException("could not compute certificate thumbprint", e);
        }
    }

    /**
     * The minimal form: the certificate itself is never needed, only its thumbprint. Useful when the public key
     * lives solely in Azure and the deployment holds nothing but the signing key.
     *
     * @param sha1Thumbprint the certificate's SHA-1 thumbprint as hex, exactly as the Azure portal displays it under
     *                       <i>Certificates &amp; secrets</i>. Spaces and colons are ignored
     */
    @Nonnull
    public static SharepointCertificateCredential of(@NonNull final PrivateKey privateKey,
                                                     @NonNull final String sha1Thumbprint) {

        final String hex = sha1Thumbprint.replaceAll("[^0-9a-fA-F]", "");

        if (hex.length() != 40) {
            throw new IllegalArgumentException(
                "a SHA-1 thumbprint has 40 hex characters, got %d: %s".formatted(hex.length(), sha1Thumbprint));
        }

        return new SharepointCertificateCredential(privateKey, encode(HexFormat.of().parseHex(hex)));
    }

    // ---------------------------------------------------------------------------------------------------------
    // pem parsing
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Parses an unencrypted PKCS#8 private key.
     *
     * <p>The JDK cannot read PKCS#1 ({@code BEGIN RSA PRIVATE KEY}) or encrypted keys without additional providers,
     * so those are rejected with an explicit hint rather than an opaque key spec error.
     */
    @Nonnull
    public static PrivateKey privateKeyFromPem(@NonNull final String privateKeyPem) {
        if (privateKeyPem.contains("BEGIN RSA PRIVATE KEY")) {
            throw new IllegalArgumentException(
                "PKCS#1 keys are not supported by the JDK - convert it first: "
                    + "openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem");
        }

        if (privateKeyPem.contains("ENCRYPTED PRIVATE KEY")) {
            throw new IllegalArgumentException(
                "encrypted private keys are not supported - decrypt it first: "
                    + "openssl pkcs8 -topk8 -nocrypt -in key.pem -out key-pkcs8.pem");
        }

        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodePem(privateKeyPem)));
        }
        catch (final Exception e) {
            throw new IllegalStateException("could not parse private key", e);
        }
    }

    @Nonnull
    public static X509Certificate certificateFromPem(@NonNull final String certificatePem) {
        try (InputStream in = new ByteArrayInputStream(certificatePem.getBytes(StandardCharsets.UTF_8))) {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
        }
        catch (final Exception e) {
            throw new IllegalStateException("could not parse certificate", e);
        }
    }

    private static byte[] decodePem(@NonNull final String pem) {
        return Base64.getDecoder().decode(
            pem.replaceAll("-----BEGIN [A-Z0-9 ]+-----", "")
                .replaceAll("-----END [A-Z0-9 ]+-----", "")
                .replaceAll("\\s", ""));
    }

    @Nonnull
    private static String resolveAlias(@NonNull final KeyStore keyStore,
                                       @NonNull final String configuredAlias) throws Exception {
        if (!configuredAlias.isEmpty()) {
            return configuredAlias;
        }

        final Enumeration<String> aliases = keyStore.aliases();

        if (!aliases.hasMoreElements()) {
            throw new IllegalStateException("keystore contains no aliases");
        }

        return aliases.nextElement();
    }

    // ---------------------------------------------------------------------------------------------------------
    // assertion
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Mints a fresh assertion. Each token request should use a new one - they carry a unique {@code jti} and a short
     * expiry.
     *
     * @param clientId         the application (client) id, used as both issuer and subject
     * @param tokenEndpointUrl the exact token endpoint the assertion is addressed to, e.g.
     *                         {@code https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/token}
     */
    @Nonnull
    public String create(@NonNull final String clientId,
                         @NonNull final String tokenEndpointUrl) {

        return create(
            SharepointClientAssertionHeader.forThumbprint(x5t),
            SharepointClientAssertionClaims.forClient(clientId, tokenEndpointUrl, LIFETIME));
    }

    /**
     * Signs an explicitly supplied header and payload - useful for tests, or to deviate from the defaults.
     */
    @Nonnull
    public String create(@NonNull final SharepointClientAssertionHeader header,
                         @NonNull final SharepointClientAssertionClaims claims) {
        try {
            final String signingInput =
                encode(objectMapper.writeValueAsBytes(header)) + "." + encode(objectMapper.writeValueAsBytes(claims));

            return signingInput + "." + encode(sign(signingInput));
        }
        catch (final RuntimeException e) {
            throw e;
        }
        catch (final Exception e) {
            throw new IllegalStateException("could not create client assertion", e);
        }
    }

    private byte[] sign(@NonNull final String signingInput) throws Exception {
        final Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(signingInput.getBytes(StandardCharsets.UTF_8));

        return signature.sign();
    }

    @Nonnull
    private static String encode(final byte[] bytes) {
        return BASE64_URL.encodeToString(bytes);
    }

}
