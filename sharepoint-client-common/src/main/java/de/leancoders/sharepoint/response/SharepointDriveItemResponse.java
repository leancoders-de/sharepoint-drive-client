package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SharepointDriveItemResponse {

    @JsonProperty("@odata.context")
    private String oDataContext;
    @JsonProperty("@odata.etag")
    private String oDataETag;

    @JsonProperty("id")
    private String id;

    @JsonProperty("createdDateTime")
    private LocalDateTime createdDateTime;

    @JsonProperty("lastModifiedDateTime")
    private LocalDateTime lastModifiedDateTime;

    @JsonProperty("name")
    private String name;

    @JsonProperty("parentReference")
    private SharepointParentReference parentReference;

    @JsonProperty("webUrl")
    private String webUrl;

    @JsonProperty("fileSystemInfo")
    private FileSystemInfo fileSystemInfo;

    @JsonProperty("folder")
    private SharepointFolderResponse folder;

    @JsonProperty("root")
    private Root root;

    @JsonProperty("size")
    private long size;


    @JsonProperty("createdBy")
    private SharepointEditor createdBy;

    @JsonProperty("cTag")
    private String cTag;

    @JsonProperty("eTag")
    private String eTag;

    @JsonProperty("lastModifiedBy")
    private SharepointEditor lastModifiedBy;

    @JsonProperty("file")
    private SharepointFile sharepointFile;

    @JsonProperty("shared")
    private Shared shared;

    @JsonProperty("@microsoft.graph.downloadUrl")
    private String microsoftGraphDownloadUrl;

    @JsonProperty("contentType")
    private ContentType contentType;

    // fields
    @JsonProperty("fields@odata.context")
    private String fieldsODataContext;
    @JsonProperty("fields")
    private Map<String, Object> fields;


    @Data
    public static class FileSystemInfo {
        @JsonProperty("createdDateTime")
        private String createdDateTime;

        @JsonProperty("lastModifiedDateTime")
        private String lastModifiedDateTime;
    }

    @Data
    public static class Root {
        // Empty class to represent the empty "root" object
    }

    @Data
    public static class Shared {
        @JsonProperty("scope")
        private String scope;
    }

    @Data
    public static class ContentType {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;
    }

}
