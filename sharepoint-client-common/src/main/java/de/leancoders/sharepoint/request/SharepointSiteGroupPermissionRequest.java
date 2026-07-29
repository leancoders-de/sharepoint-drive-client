package de.leancoders.sharepoint.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Body for {@code POST /drives/{driveId}/items/{itemId}/permissions}.
 *
 * <p>Assigns a SharePoint (site) group to a drive item via its {@code principalId} + title. This shape only
 * grants a site group on a <b>SharePoint Embedded container</b>; on a regular SharePoint Online document
 * library the same endpoint only accepts an <i>application</i> permission.
 *
 * <pre>
 * { "grantedToV2": { "siteGroup": { "id": "&lt;principalId&gt;", "displayName": "&lt;name&gt;" } }, "roles": ["write"] }
 * </pre>
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/driveitem-post-permissions">driveItem: create permission</a>
 */
@Data
public class SharepointSiteGroupPermissionRequest {

    @JsonProperty("grantedToV2")
    private GrantedToV2 grantedToV2;

    @JsonProperty("roles")
    private List<String> roles;

    @Data
    public static class GrantedToV2 {
        @JsonProperty("siteGroup")
        private SiteGroup siteGroup;
    }

    @Data
    public static class SiteGroup {
        // the site group's principalId (integer, unique within the site)
        @JsonProperty("id")
        private String id;

        // the site group's title
        @JsonProperty("displayName")
        private String displayName;
    }

}
