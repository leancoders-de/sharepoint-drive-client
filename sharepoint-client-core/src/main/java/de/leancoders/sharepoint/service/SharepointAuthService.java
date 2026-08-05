package de.leancoders.sharepoint.service;

import lombok.NonNull;

import javax.annotation.Nonnull;

/**
 * Supplies app-only access tokens, independent of how the application proves its identity.
 *
 * <p>Two implementations:
 * <ul>
 *     <li>{@link SharepointClientService} - client secret. Accepted by Microsoft Graph, <b>rejected</b> by the
 *     classic SharePoint REST API</li>
 *     <li>{@link SharepointCertificateClientService} - certificate based client assertion. Accepted by both</li>
 * </ul>
 *
 * <p>Tokens are audience bound, so a token minted for one resource cannot be replayed against another. Callers name
 * the resource they need; implementations cache one context per audience.
 */
public interface SharepointAuthService {

    /**
     * Returns a valid token for the given audience, minting one on first use and whenever the cached one expired.
     *
     * @param resourceRoot scheme and host of the resource, e.g. {@code https://tenant.sharepoint.com} or
     *                     {@code https://graph.microsoft.com}
     */
    @Nonnull
    SharepointAuthContext validateAndGetForResource(@NonNull final String resourceRoot);

    /**
     * Convenience for the most common audience, Microsoft Graph.
     */
    @Nonnull
    default SharepointAuthContext validateAndGet(@NonNull final String graphResource) {
        return validateAndGetForResource(graphResource);
    }

}
