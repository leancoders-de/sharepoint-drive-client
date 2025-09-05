package de.leancoders.sharepoint.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointTokenResponse {

    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private int expiresInSeconds;
    @JsonProperty("ext_expires_in")
    private int extExpiresInSeconds;
    @JsonProperty("access_token")
    private String accessToken;

}
