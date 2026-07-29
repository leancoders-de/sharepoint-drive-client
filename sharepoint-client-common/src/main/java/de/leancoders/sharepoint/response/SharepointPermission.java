package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * A {@code permission} object as returned by the driveItem permissions endpoints.
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/resources/permission">permission resource</a>
 */
@Data
public class SharepointPermission {

    @JsonProperty("@odata.context")
    private String oDataContext;

    @JsonProperty("id")
    private String id;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("grantedToV2")
    private GrantedToV2 grantedToV2;

    @Data
    public static class GrantedToV2 {
        @JsonProperty("siteGroup")
        private SiteGroup siteGroup;

        @JsonProperty("sharePointGroup")
        private SharePointGroup sharePointGroup;
    }

    @Data
    public static class SiteGroup {
        @JsonProperty("id")
        private String id;

        @JsonProperty("displayName")
        private String displayName;
    }

    @Data
    public static class SharePointGroup {
        @JsonProperty("id")
        private String id;

        @JsonProperty("principalId")
        private String principalId;

        @JsonProperty("title")
        private String title;
    }

}
