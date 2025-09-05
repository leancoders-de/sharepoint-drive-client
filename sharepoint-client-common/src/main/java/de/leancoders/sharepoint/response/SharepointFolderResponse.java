package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointFolderResponse {
    @JsonProperty("childCount")
    private int childCount;
}
