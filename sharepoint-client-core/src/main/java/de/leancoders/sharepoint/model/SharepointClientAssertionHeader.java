package de.leancoders.sharepoint.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;

import javax.annotation.Nonnull;

/**
 * JOSE header of the client assertion JWT presented to Entra instead of a client secret.
 *
 * <pre>
 * {"alg":"RS256","typ":"JWT","x5t":"&lt;base64url sha-1 thumbprint&gt;"}
 * </pre>
 *
 * @param alg signing algorithm - Entra requires {@code RS256} for certificate credentials
 * @param typ always {@code JWT}
 * @param x5t base64url encoded SHA-1 thumbprint identifying which of the app registration's certificates signed this
 *
 * @see <a href="https://learn.microsoft.com/en-us/entra/identity-platform/certificate-credentials">certificate credentials</a>
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SharepointClientAssertionHeader(

    @JsonProperty("alg")
    String alg,

    @JsonProperty("typ")
    String typ,

    @JsonProperty("x5t")
    String x5t

) {

    public static final String ALGORITHM_RS256 = "RS256";
    public static final String TYPE_JWT = "JWT";

    @Nonnull
    public static SharepointClientAssertionHeader forThumbprint(@NonNull final String x5t) {
        return SharepointClientAssertionHeader.builder()
            .alg(ALGORITHM_RS256)
            .typ(TYPE_JWT)
            .x5t(x5t)
            .build();
    }

}
