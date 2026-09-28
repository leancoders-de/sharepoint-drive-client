package de.leancoders.sharepoint.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Body for {@code POST /drives/{driveId}/items/{itemId}/copy}.
 *
 * <pre>
 * {
 *   "parentReference": {
 *     "driveId": "b!s8RqPCGh0ESQS2EYnKM0IKS3lM7GxjdAviiob7oc5pXv_0LiL-62Qq3IXyrXnEop",
 *     "id": "DCD0D3AD-8989-4F23-A5A2-2C086050513F"
 *   },
 *   "name": "contoso plan (copy).txt"
 * }
 * </pre>
 *
 * <p>Every property is optional - an empty body copies the item next to itself under the same name, which fails
 * unless a conflict behavior is given. Unset properties must stay out of the payload, hence
 * {@link JsonInclude.Include#NON_NULL} and the boxed booleans.
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/driveitem-copy?view=graph-rest-1.0">driveItem: copy</a>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SharepointCopyRequest {

    /**
     * The target folder. Graph wants {@code driveId} and {@code id}; {@code path} works as an alternative.
     */
    @JsonProperty("parentReference")
    private SharepointItemReference parentReference;

    /**
     * The name of the copy. Defaults to the source name.
     *
     * <p>Must not be combined with {@link #childrenOnly}, and silently cancels
     * {@link #includeAllVersionHistory} - copy first, rename afterwards if both are needed.
     */
    @JsonProperty("name")
    private String name;

    /**
     * Copies the contents of a folder rather than the folder itself. Folders only, and the only way to copy
     * out of a drive root.
     */
    @JsonProperty("childrenOnly")
    private Boolean childrenOnly;

    /**
     * Carries major and minor versions over to the target, within the target site's version limit.
     * Without it only the latest major version is copied.
     */
    @JsonProperty("includeAllVersionHistory")
    private Boolean includeAllVersionHistory;

    /**
     * Reference to the folder a copy is created in. Only the addressing properties are modelled - the response
     * side lives in {@link de.leancoders.sharepoint.response.SharepointParentReference}.
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SharepointItemReference {

        @JsonProperty("driveId")
        private String driveId;

        @JsonProperty("id")
        private String id;

        /**
         * Drive relative path, e.g. {@code /drive/root:/Documents}. An alternative to {@link #id}.
         */
        @JsonProperty("path")
        private String path;
    }
}
