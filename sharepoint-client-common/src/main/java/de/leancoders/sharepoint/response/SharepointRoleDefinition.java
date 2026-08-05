package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A SharePoint permission level as returned by {@code GET {siteUrl}/_api/web/roledefinitions}.
 *
 * <p>Note the PascalCase property names - this is the classic SharePoint REST API, not Microsoft Graph.
 *
 * <p>{@link #name} is <b>localized</b> (a German site returns {@code Vollzugriff} rather than
 * {@code Full Control}), so always match on {@link #roleTypeKind} instead.
 *
 * @see <a href="https://learn.microsoft.com/en-us/previous-versions/office/sharepoint-server/ee536458(v=office.15)">RoleType enumeration</a>
 */
@Data
public class SharepointRoleDefinition {

    /**
     * SharePoint's built-in {@code RoleType} values. Custom permission levels report {@code 0} (None).
     */
    public static final int ROLE_TYPE_GUEST = 1;
    public static final int ROLE_TYPE_READER = 2;
    public static final int ROLE_TYPE_CONTRIBUTOR = 3;
    public static final int ROLE_TYPE_WEB_DESIGNER = 4;
    public static final int ROLE_TYPE_ADMINISTRATOR = 5;
    public static final int ROLE_TYPE_EDITOR = 6;

    /**
     * The {@code roledefid} argument for {@code addroleassignment} / {@code removeroleassignment}.
     */
    @JsonProperty("Id")
    private int id;

    /**
     * Localized display name, e.g. {@code Vollzugriff}. Do not match on this.
     */
    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description;

    @JsonProperty("RoleTypeKind")
    private int roleTypeKind;

    @JsonProperty("Hidden")
    private boolean hidden;

    @JsonProperty("Order")
    private int order;

}
