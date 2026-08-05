package de.leancoders.sharepoint.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Location of the PKCS#12 keystore holding the private key and certificate used for SharePoint app-only
 * authentication.
 *
 * <p>Deliberately separate from {@link SharepointConfig} so the Graph client secret flow stays untouched - only the
 * SharePoint REST API requires a certificate.
 *
 * <p>The matching public key must be uploaded to the app registration under <i>Certificates &amp; secrets</i> -
 * <i>Certificates</i>.
 */
@AllArgsConstructor(staticName = "of")
@Getter
@ToString(exclude = "keyStorePassword")
@EqualsAndHashCode
public class SharepointCertificateConfig {

    /**
     * Path to the PKCS#12 keystore (.pfx / .p12).
     */
    @NonNull
    private final String keyStorePath;

    @NonNull
    private final String keyStorePassword;

    /**
     * Alias of the key entry. Pass an empty string to use the keystore's first alias.
     */
    @NonNull
    private final String keyAlias;

    @NonNull
    public static SharepointCertificateConfig of(@NonNull final String keyStorePath,
                                                 @NonNull final String keyStorePassword) {
        return of(keyStorePath, keyStorePassword, "");
    }

}
