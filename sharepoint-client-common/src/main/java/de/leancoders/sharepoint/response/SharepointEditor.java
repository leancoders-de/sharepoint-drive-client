package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointEditor {

    @JsonProperty("user")
    private SharepointUser user;

}
