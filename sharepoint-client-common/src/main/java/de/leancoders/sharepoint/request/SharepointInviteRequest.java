package de.leancoders.sharepoint.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Body for {@code POST /drives/{drive-id}/items/{item-id}/invite}.
 *
 * <p>Assigns a SharePoint (site) group to a drive item.
 *
 * <pre>
 * {
 *   "requireSignIn": false,
 *   "sendInvitation": false,
 *   "roles": [ "read | write"],
 *   "recipients": [
 *     { "@odata.type": "microsoft.graph.driveRecipient" },
 *     { "@odata.type": "microsoft.graph.driveRecipient" }
 *   ],
 *   "message": "string"
 * }
 *
 * "@odata.type": "microsoft.graph.driveRecipient":
 * {
 *   "alias": "string",
 *   "email": "string",
 *   "objectId": "string",
 * }
 * </pre>
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/driveitem-invite?view=graph-rest-1.0&tabs=http">invite</a>
 */
@Data
public class SharepointInviteRequest {

    @JsonProperty("requireSignIn")
    private boolean requireSignIn;
    @JsonProperty("sendInvitation")
    private boolean sendInvitation;
    @JsonProperty("message")
    private String message;

    @JsonProperty("roles")
    private List<String> roles;

    @JsonProperty("recipients")
    private List<Recipient> recipients;

    @Data
    public static class Recipient {
        @JsonProperty("alias")
        private String alias;
        @JsonProperty("email")
        private String email;
        @JsonProperty("objectId")
        private String objectId;
    }

}
