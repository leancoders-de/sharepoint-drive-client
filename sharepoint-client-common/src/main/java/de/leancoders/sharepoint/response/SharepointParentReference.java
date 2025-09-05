package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointParentReference {

    @JsonProperty("driveType")
    private String driveType;

    @JsonProperty("driveId")
    private String driveId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("path")
    private String path;

    @JsonProperty("siteId")
    private String siteId;

}