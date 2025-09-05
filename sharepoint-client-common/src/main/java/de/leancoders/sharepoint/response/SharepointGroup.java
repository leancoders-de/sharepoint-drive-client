package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointGroup {
    @JsonProperty("id")
    private String id;

    @JsonProperty("displayName")
    private String displayName;
}