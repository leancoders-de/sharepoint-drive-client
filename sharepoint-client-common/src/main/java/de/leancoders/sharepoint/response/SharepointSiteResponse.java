package de.leancoders.sharepoint.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SharepointSiteResponse {

    @JsonProperty("@odata.context")
    private String context;
    @JsonProperty("id")
    private String id;
    @JsonProperty("createdDateTime")
    private LocalDateTime createdDateTime;
    @JsonProperty("lastModifiedDateTime")
    private LocalDateTime lastModifiedDateTime;
    @JsonProperty("description")
    private String description;
    @JsonProperty("name")
    private String name;
    @JsonProperty("displayName")
    private String displayName;
    @JsonProperty("webUrl")
    private String webUrl;
    @JsonProperty("siteCollection")
    private SharepointSiteCollection siteCollection;

}
