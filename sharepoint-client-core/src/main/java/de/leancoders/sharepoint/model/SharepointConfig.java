package de.leancoders.sharepoint.model;


import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;


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

    //
    @NonNull
    private final String appClientId;
    @NonNull
    private final String appClientSecret;
    @NonNull
    private final String appTenantId;

}
