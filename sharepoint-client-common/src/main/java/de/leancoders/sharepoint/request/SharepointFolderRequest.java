package de.leancoders.sharepoint.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointFolderRequest {

    // "@microsoft.graph.conflictBehavior": "rename"
    @JsonProperty("@microsoft.graph.conflictBehavior")
    private String conflictBehavior;

    @JsonProperty("name")
    private String name;

    @JsonProperty("folder")
    private SharepointFolderUpdateItem folder;


    @Data
    public static class SharepointFolderUpdateItem {
        @JsonProperty("random")
        private String random;
    }

}
