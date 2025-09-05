package de.leancoders.sharepoint.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SharepointDriveResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("createdDateTime")
    private LocalDateTime createdDateTime;

    @JsonProperty("lastModifiedDateTime")
    private LocalDateTime lastModifiedDateTime;

    @JsonProperty("name")
    private String name;

    @JsonProperty("webUrl")
    private String webUrl;

    @JsonProperty("driveType")
    private String driveType;

    @JsonProperty("createdBy")
    private SharepointEditor createdBy;

    @JsonProperty("lastModifiedBy")
    private SharepointEditor lastModifiedBy;

    @JsonProperty("owner")
    private SharepointOwner owner;

    @JsonProperty("quota")
    private SharepointDriveQuota quota;


}
