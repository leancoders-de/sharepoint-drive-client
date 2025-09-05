package de.leancoders.sharepoint.service;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@AllArgsConstructor(staticName = "of")
@Data
@ToString
public class SharepointAuthContext {

    private final boolean authenticated;

    @Nonnull
    private final LocalDateTime tokenValidUntil;
    @NonNull
    private final String tenantId;
    @NonNull
    private final String clientId;
    @NonNull
    private final String clientSecret;
    @NonNull
    private final SharepointTokenResponse token;
    @NonNull
    private final Supplier<RequestSpecification> requestSpecification;

    @Nonnull
    public static SharepointAuthContext success(@NonNull final String tenantId,
                                                @NonNull final String clientId,
                                                @NonNull final String clientSecret,
                                                @NonNull final SharepointTokenResponse token,
                                                @NonNull final Supplier<RequestSpecification> requestSpecification) {
        final LocalDateTime tokenValidUntil = LocalDateTime.now().plusSeconds(token.getExpiresInSeconds());

        return SharepointAuthContext.of(true, tokenValidUntil, tenantId, clientId, clientSecret, token, requestSpecification);
    }

    @Nonnull
    public static SharepointAuthContext empty() {
        return SharepointAuthContext.of(false, LocalDateTime.MIN, "", "", "", new SharepointTokenResponse(), RestAssured::given);
    }

    public boolean isAuthenticated() {
        final String accessToken = token.getAccessToken();
        return authenticated
            && accessToken != null
            && tokenValidUntil.isAfter(LocalDateTime.now().plusSeconds(30));
    }

    @Nonnull
    public RequestSpecification request() {
        if (isAuthenticated()) {
            return authorizedRequest();
        }
        else {
            return unauthorizedRequest();
        }
    }

    @Nonnull
    public RequestSpecification authorizedRequest() {
        final String accessToken = token.getAccessToken();

        return requestSpecification.get()
            .accept(ContentType.JSON)
            .header("Authorization", "Bearer " + accessToken);
    }

    @Nonnull
    public RequestSpecification unauthorizedRequest() {
        return requestSpecification.get()
            .accept(ContentType.JSON);
    }
}
