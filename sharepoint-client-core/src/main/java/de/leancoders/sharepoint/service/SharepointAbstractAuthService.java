package de.leancoders.sharepoint.service;

import com.google.common.collect.Maps;
import de.leancoders.sharepoint.helper.ObjectMapperFactory;
import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.model.SharepointTokenResponse;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Everything the two {@link SharepointAuthService} implementations share: the token endpoint, the per-audience
 * cache, and the scope construction. Subclasses only decide how the client authenticates itself.
 */
public abstract class SharepointAbstractAuthService implements SharepointAuthService, SharepointPaths {

    @Nonnull
    protected static final RestAssuredConfig REST_ASSURED_CONFIG =
        RestAssuredConfig.config()
            .objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory(
                    (type, s) -> ObjectMapperFactory.createDefaultObjectMapper()
                ));

    private static final String SCOPE_SUFFIX = "/.default";

    @NonNull
    protected final SharepointConfig config;

    /**
     * One context per audience, keyed by resource root.
     */
    @Nonnull
    private final Map<String, SharepointAuthContext> authContexts = Maps.newConcurrentMap();

    protected SharepointAbstractAuthService(@NonNull final SharepointConfig config) {
        this.config = config;
    }

    @Override
    @Nonnull
    public SharepointAuthContext validateAndGetForResource(@NonNull final String resourceRoot) {
        final SharepointAuthContext cached = authContexts.get(resourceRoot);

        if (cached == null || !cached.isAuthenticated()) {
            return obtainAccessToken(resourceRoot);
        }

        return cached;
    }

    @Nonnull
    protected SharepointAuthContext obtainAccessToken(@NonNull final String resourceRoot) {

        final SharepointTokenResponse token = requestToken(resourceRoot + SCOPE_SUFFIX);

        final SharepointAuthContext authContext =
            SharepointAuthContext.success(
                config.getAppTenantId(),
                config.getAppClientId(),
                config.getAppClientSecret(),
                token,
                this::given);

        authContexts.put(resourceRoot, authContext);

        return authContext;
    }

    /**
     * Performs the token request, presenting whichever credential the implementation holds.
     *
     * @param scope the full scope, e.g. {@code https://graph.microsoft.com/.default}
     */
    @Nonnull
    protected abstract SharepointTokenResponse requestToken(@NonNull final String scope);

    @Nonnull
    protected RequestSpecification authGiven() {
        return RestAssured.given()
            .port(config.getAuthPort())
            .baseUri(config.getAuthUri())
            .config(REST_ASSURED_CONFIG);
    }

    /**
     * The request specification handed to callers through {@link SharepointAuthContext}. Defaults to no base uri,
     * because callers that talk to more than one host supply absolute urls themselves; override to pin one.
     */
    @Nonnull
    protected RequestSpecification given() {
        return RestAssured.given()
            .config(REST_ASSURED_CONFIG);
    }

    /**
     * The exact token endpoint, needed both to post to and - for the certificate flow - as the assertion's audience.
     */
    @Nonnull
    protected String tokenEndpointUrl() {
        final String authUri = config.getAuthUri();
        final String base = authUri.endsWith("/") ? authUri.substring(0, authUri.length() - 1) : authUri;

        return "%s/%s/oauth2/v2.0/token".formatted(base, config.getAppTenantId());
    }

}
