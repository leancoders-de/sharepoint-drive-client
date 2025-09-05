package de.leancoders.sharepoint.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * {
 * "@odata.context": "https://graph.microsoft.com/v1.0/$metadata#sites/$entity",
 * "createdDateTime": "2024-11-05T11:32:15.353Z",
 * "description": "",
 * "id": "leancodersde.sharepoint.com,2a58be08-8d56-44ed-942b-6e892404f8fc,2bd44f8e-c688-4990-9c4a-6b331c928b12",
 * "lastModifiedDateTime": "2025-04-17T15:45:56Z",
 * "name": "",
 * "webUrl": "https://leancodersde.sharepoint.com",
 * "displayName": "Kommunikationswebsite",
 * "root": {},
 * "siteCollection": {
 * "hostname": "leancodersde.sharepoint.com"
 * }
 * }
 */
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
