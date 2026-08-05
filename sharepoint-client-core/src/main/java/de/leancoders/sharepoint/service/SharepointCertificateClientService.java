package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.helper.SharepointCertificateCredential;
import de.leancoders.sharepoint.model.SharepointCertificateConfig;
import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.model.SharepointTokenResponse;
import io.restassured.http.ContentType;
import lombok.NonNull;

import javax.annotation.Nonnull;

/**
 * Certificate based app-only authentication.
 *
 * <p>Accepted by every audience: Microsoft Graph takes it just as happily as a client secret, and the classic
 * SharePoint REST API takes <i>only</i> this - it rejects secret based tokens ({@code appidacr=1}) with
 * <i>Unsupported app only token</i> and requires a client assertion ({@code appidacr=2}).
 *
 * <p>Apart from the signing key everything is shared with {@link SharepointClientService}: same tenant, same app
 * registration, same token endpoint. To reach SharePoint the app registration additionally needs the
 * <b>SharePoint</b> application permission {@code Sites.FullControl.All} (resource
 * {@code 00000003-0000-0ff1-ce00-000000000000}) with admin consent, and the certificate's public key uploaded under
 * <i>Certificates &amp; secrets</i>. Reaching Graph with it needs no extra setup at all - permissions are granted
 * per resource, not per credential type.
 */
public class SharepointCertificateClientService extends SharepointAbstractAuthService {

    @NonNull
    private final SharepointCertificateCredential credential;

    /**
     * Convenience for the PKCS#12 keystore case.
     */
    public SharepointCertificateClientService(@NonNull final SharepointConfig config,
                                              @NonNull final SharepointCertificateConfig certificateConfig) {
        super(config);

        this.credential = SharepointCertificateCredential.fromKeyStore(certificateConfig);
    }

    /**
     * Takes a pre-built credential, so the key material can come from anywhere - PEM strings out of a secret manager,
     * a hardware backed {@link java.security.PrivateKey}, or just a key plus the thumbprint shown in the Azure portal.
     *
     * @see SharepointCertificateCredential#fromPem(String, String)
     * @see SharepointCertificateCredential#of(java.security.PrivateKey, String)
     */
    public SharepointCertificateClientService(@NonNull final SharepointConfig config) {
        super(config);

        this.credential = SharepointCertificateCredential.fromPem(
            config.getKeyFilePath(),
            config.getCertFilePath()
        );
    }

    @Override
    @Nonnull
    protected SharepointTokenResponse requestToken(@NonNull final String scope) {

        final String assertion = credential.create(config.getAppClientId(), tokenEndpointUrl());

        return authGiven()
            .contentType(ContentType.URLENC)
            .formParam("client_id", config.getAppClientId())
            .formParam("scope", scope)
            .formParam("client_assertion_type", SharepointCertificateCredential.CLIENT_ASSERTION_TYPE)
            .formParam("client_assertion", assertion)
            .formParam("grant_type", "client_credentials")
            // deliberately not logged - the assertion is a signing credential
            .expect().statusCode(200)
            .when()
            .post(OAUTH_AUTH__TOKEN, config.getAppTenantId())
            .as(SharepointTokenResponse.class);
    }
}
