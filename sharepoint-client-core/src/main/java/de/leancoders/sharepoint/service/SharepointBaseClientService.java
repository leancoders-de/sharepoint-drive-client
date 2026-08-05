package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.model.SharepointConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class SharepointBaseClientService {

    @NonNull
    protected final SharepointConfig config;
    /**
     * Either credential type works here - Graph accepts both a client secret and a certificate.
     */
    @NonNull
    protected final SharepointAuthService clientService;

    @Nonnull
    protected SharepointAuthContext authContext() {
        return clientService.validateAndGet(config.getGraphUri());
    }


}
