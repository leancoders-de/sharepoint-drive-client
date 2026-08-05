package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.model.SharepointTokenResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.NonNull;

import javax.annotation.Nonnull;

/**
 * Client secret based app-only authentication.
 *
 * <p>Fine for Microsoft Graph. <b>Not usable against the classic SharePoint REST API</b>, which inspects the
 * {@code appidacr} claim and rejects anything obtained with a secret ({@code appidacr=1}) with <i>Unsupported app
 * only token</i> - use {@link SharepointCertificateClientService} there.
 */
public class SharepointClientService extends SharepointAbstractAuthService {

    public SharepointClientService(@Nonnull final SharepointConfig config) {
        super(config);
    }

    @Override
    @Nonnull
    protected SharepointTokenResponse requestToken(@NonNull final String scope) {
        return authGiven()
            .contentType(ContentType.URLENC)
            .formParam("client_id", config.getAppClientId())
            .formParam("scope", scope)
            .formParam("client_secret", config.getAppClientSecret())
            .formParam("grant_type", "client_credentials")
            .log().all()
            .expect().statusCode(200)
            .log().all()
            .when()
            .post(OAUTH_AUTH__TOKEN, config.getAppTenantId())
            .as(SharepointTokenResponse.class);
    }

    /**
     * Pins the Graph host, so callers may issue relative paths.
     */
    @Override
    @Nonnull
    protected RequestSpecification given() {
        return RestAssured.given()
            .port(config.getGraphPort())
            .baseUri(config.getGraphUri())
            .config(REST_ASSURED_CONFIG);
    }
}
