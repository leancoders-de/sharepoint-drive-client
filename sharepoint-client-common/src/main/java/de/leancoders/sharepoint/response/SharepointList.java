package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointList {

    @JsonProperty("@odata.etag")
    private String odataEtag;

    @JsonProperty("createdDateTime")
    private String createdDateTime;

    @JsonProperty("description")
    private String description;

    @JsonProperty("eTag")
    private String eTag;

    @JsonProperty("id")
    private String id;

    @JsonProperty("lastModifiedDateTime")
    private String lastModifiedDateTime;

    @JsonProperty("name")
    private String name;

    @JsonProperty("webUrl")
    private String webUrl;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("createdBy")
    private SharepointEditor createdBy;

    @JsonProperty("parentReference")
    private SharepointParentReference parentReference;

    @JsonProperty("list")
    private ListDetails listDetails;


    @Data
    public static class ListDetails {
        @JsonProperty("contentTypesEnabled")
        private boolean contentTypesEnabled;

        @JsonProperty("hidden")
        private boolean hidden;

        @JsonProperty("template")
        private String template;
    }
}
