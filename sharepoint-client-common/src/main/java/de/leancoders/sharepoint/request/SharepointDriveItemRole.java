package de.leancoders.sharepoint.request;

import de.leancoders.sharepoint.response.SharepointRoleDefinition;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Roles that can be granted on a SharePoint drive item permission.
 *
 * <p>Carries both representations: {@link #value} for Microsoft Graph ({@code /invite}), and
 * {@link #roleTypeKind} for the classic SharePoint REST API, where a role is identified by a site-specific
 * {@code roledefid} that has to be looked up from {@code _api/web/roledefinitions} via its {@code RoleTypeKind}.
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/driveitem-post-permissions">driveItem: create permission</a>
 */
@Getter
@RequiredArgsConstructor
public enum SharepointDriveItemRole {

    /**
     * SharePoint permission level "Read" / "Lesen".
     */
    READ("read", SharepointRoleDefinition.ROLE_TYPE_READER),
    /**
     * SharePoint permission level "Contribute" / "Mitwirken" - add, edit and delete items.
     *
     * <p>Use {@link SharepointRoleDefinition#ROLE_TYPE_EDITOR} instead if the principal should additionally be
     * allowed to create and delete lists; that is what the built-in Members group gets.
     */
    WRITE("write", SharepointRoleDefinition.ROLE_TYPE_CONTRIBUTOR),
    /**
     * SharePoint permission level "Full Control" / "Vollzugriff".
     *
     * <p>Not grantable through Graph's {@code /invite} on a regular document library - only via the SharePoint
     * REST API.
     */
    OWNER("owner", SharepointRoleDefinition.ROLE_TYPE_ADMINISTRATOR);

    private final String value;

    /**
     * The SharePoint {@code RoleTypeKind} this role maps to. Match role definitions on this rather than on their
     * name, which is localized.
     */
    private final int roleTypeKind;

    @Override
    public String toString() {
        return value;
    }
}
