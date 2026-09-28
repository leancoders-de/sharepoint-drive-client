package de.leancoders.sharepoint.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * How Microsoft Graph should resolve a name clash in the target folder.
 *
 * <p>Passed as the {@code @microsoft.graph.conflictBehavior} query parameter on
 * {@code POST /drives/{driveId}/items/{itemId}/copy}, and as a body property on the item creation calls.
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/driveitem-copy?view=graph-rest-1.0">driveItem: copy</a>
 */
@Getter
@RequiredArgsConstructor
public enum SharepointConflictBehavior {

    /**
     * The whole operation fails on the first conflict. Graph's default when nothing is specified.
     */
    FAIL("fail"),
    /**
     * Appends the lowest integer that makes the name unique.
     */
    RENAME("rename"),
    /**
     * Deletes the pre-existing item - including its version history - and puts the copy in its place.
     *
     * <p>Only honoured for files: a conflicting <i>folder</i> falls back to {@link #FAIL}, so a
     * {@code childrenOnly} copy of a folder that contains subfolders fails with {@code nameAlreadyExists}
     * despite this setting.
     */
    REPLACE("replace");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
