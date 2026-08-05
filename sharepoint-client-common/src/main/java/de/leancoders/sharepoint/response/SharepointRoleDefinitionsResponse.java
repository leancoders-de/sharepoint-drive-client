package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Wrapper for {@code GET {siteUrl}/_api/web/roledefinitions} when requested with
 * {@code Accept: application/json;odata=nometadata}.
 */
@Data
public class SharepointRoleDefinitionsResponse {

    @JsonProperty("value")
    private List<SharepointRoleDefinition> value;

}
