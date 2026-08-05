package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Wrapper for {@code GET {siteUrl}/_api/web/sitegroups} when requested with
 * {@code Accept: application/json;odata=nometadata}.
 */
@Data
public class SharepointSiteGroupsResponse {

    @JsonProperty("value")
    private List<SharepointSiteGroup> value;

}
