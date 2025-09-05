package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointOwner {

    @JsonProperty("group")
    private SharepointGroup group;

    @JsonProperty("user")
    private SharepointUser user;

}
