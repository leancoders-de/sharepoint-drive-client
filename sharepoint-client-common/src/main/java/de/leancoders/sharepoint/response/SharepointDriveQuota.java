package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointDriveQuota {
    @JsonProperty("deleted")
    private long deleted;

    @JsonProperty("remaining")
    private long remaining;

    @JsonProperty("state")
    private String state;

    @JsonProperty("total")
    private long total;

    @JsonProperty("used")
    private long used;
}
