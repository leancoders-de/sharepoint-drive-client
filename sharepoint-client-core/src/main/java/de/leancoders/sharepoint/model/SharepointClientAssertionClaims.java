package de.leancoders.sharepoint.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Payload of the client assertion JWT presented to Entra instead of a client secret.
 *
 * <pre>
 * {"aud":"https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/token",
 *  "iss":"{clientId}","sub":"{clientId}","jti":"...","nbf":...,"exp":...}
 * </pre>
 *
 * @param aud the exact token endpoint this assertion is addressed to
 * @param iss the application (client) id
 * @param sub the application (client) id - same as the issuer for client credentials
 * @param jti unique id, so a captured assertion cannot be replayed
 * @param nbf not valid before, epoch seconds
 * @param exp expiry, epoch seconds - Entra rejects assertions living much longer than ten minutes
 *
 * @see <a href="https://learn.microsoft.com/en-us/entra/identity-platform/certificate-credentials">certificate credentials</a>
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SharepointClientAssertionClaims(

    @JsonProperty("aud")
    String aud,

    @JsonProperty("iss")
    String iss,

    @JsonProperty("sub")
    String sub,

    @JsonProperty("jti")
    String jti,

    @JsonProperty("nbf")
    long nbf,

    @JsonProperty("exp")
    long exp

) {

    /**
     * Builds a fresh set of claims with a random {@code jti} and a validity window starting now.
     *
     * @param clientId         used as both issuer and subject
     * @param tokenEndpointUrl must name the token endpoint the assertion is posted to
     * @param lifetime         how long the assertion stays valid
     */
    @Nonnull
    public static SharepointClientAssertionClaims forClient(@NonNull final String clientId,
                                                            @NonNull final String tokenEndpointUrl,
                                                            @NonNull final Duration lifetime) {
        final long now = Instant.now().getEpochSecond();

        return SharepointClientAssertionClaims.builder()
            .aud(tokenEndpointUrl)
            .iss(clientId)
            .sub(clientId)
            .jti(UUID.randomUUID().toString())
            .nbf(now)
            .exp(now + lifetime.getSeconds())
            .build();
    }

}
