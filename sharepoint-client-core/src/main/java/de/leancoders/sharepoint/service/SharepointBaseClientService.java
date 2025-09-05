package de.leancoders.sharepoint.service;

import com.google.common.base.Joiner;
import de.leancoders.sharepoint.model.SharepointConfig;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class SharepointBaseClientService {

    @NonNull
    protected final SharepointConfig config;
    @NonNull
    protected final SharepointClientService clientService;

    protected static final Joiner REF_ID_JOINER = Joiner.on(",").skipNulls();

    @Nonnull
    protected SharepointAuthContext authContext() {
        return clientService.validateAndGet();
    }

    @Nonnull
    protected String encode(@NonNull final String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

}
