package de.leancoders.sharepoint.model;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.nio.file.Path;


@AllArgsConstructor(staticName = "of")
@Getter
@ToString
@EqualsAndHashCode
@Log4j2
public class SharepointConfig {

    @NonNull
    private final String authUri;
    private final int authPort;
    @NonNull
    private final String graphUri;
    private final int graphPort;

    // native app credentials auth
    @NonNull
    private final String appClientId;
    @NonNull
    private final String appClientSecret;
    @NonNull
    private final String appTenantId;

    // certificate based auth
    @NonNull
    private final Path keyFilePath;
    @NonNull
    private final Path certFilePath;
}
