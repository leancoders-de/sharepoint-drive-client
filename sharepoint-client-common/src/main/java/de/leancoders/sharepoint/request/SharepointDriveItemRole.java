package de.leancoders.sharepoint.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Roles that can be granted on a SharePoint drive item permission.
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/driveitem-post-permissions">driveItem: create permission</a>
 */
@Getter
@RequiredArgsConstructor
public enum SharepointDriveItemRole {

    READ("read"),
    WRITE("write"),
    /**
     * May be rejected on SharePoint Embedded containers (the docs only demonstrate read/write) - verify per tenant.
     */
    OWNER("owner");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
