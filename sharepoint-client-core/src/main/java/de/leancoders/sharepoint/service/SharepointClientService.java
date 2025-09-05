package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.helper.ObjectMapperFactory;
import de.leancoders.sharepoint.model.SharepointConfig;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.NonNull;

import javax.annotation.Nonnull;

public class SharepointClientService {

    @Nonnull
    private static final RestAssuredConfig REST_ASSURED_CONFIG =
        RestAssuredConfig.config()
            .objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory(
                    (type, s) -> ObjectMapperFactory.createDefaultObjectMapper()
                ));

    @NonNull
    private final SharepointConfig config;
    @NonNull
    private SharepointAuthContext authContext;

    public SharepointClientService(@Nonnull final SharepointConfig config) {
        this.config = config;
        this.authContext = SharepointAuthContext.empty();
    }

    @Nonnull
    protected SharepointAuthContext obtainAccessToken() {
        return obtainAccessToken(
            config.getAppTenantId(),
            config.getAppClientId(),
            config.getAppClientSecret(),
            SharepointPaths.OAUTH_AUTH__TOKEN
        );
    }

    @Nonnull
    protected SharepointAuthContext obtainAccessToken(@NonNull final String tenantId,
                                                      @NonNull final String clientId,
                                                      @NonNull final String clientSecret,
                                                      @NonNull final String path) {

        final SharepointTokenResponse token =
            given()
                .contentType(ContentType.URLENC)
                .formParam("client_id", clientId)
                .formParam("scope", "https://graph.microsoft.com/.default")
                .formParam("client_secret", clientSecret)
                .formParam("grant_type", "client_credentials")
                .log().all()
                .expect().statusCode(200)
                .log().all()
                .when()
                .post(path, tenantId)
                .as(SharepointTokenResponse.class);

        this.authContext = SharepointAuthContext.success(tenantId, clientId, clientSecret, token, this::given);

        return this.authContext;
    }

    @Nonnull
    private RequestSpecification given() {
        return RestAssured.given()
            .port(config.getAuthPort())
            .baseUri(config.getAuthUri())
            .config(REST_ASSURED_CONFIG);
    }

    @Nonnull
    public SharepointAuthContext validateAndGet() {
        if (!authContext.isAuthenticated()) {
            obtainAccessToken();
        }

        return authContext;
    }
}
