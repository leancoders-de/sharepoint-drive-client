package de.leancoders.sharepoint.service;

import com.google.common.collect.ImmutableList;
import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.request.SharepointDriveItemRole;
import de.leancoders.sharepoint.request.SharepointFolderRequest;
import de.leancoders.sharepoint.request.SharepointInviteRequest;
import de.leancoders.sharepoint.request.SharepointSiteGroupPermissionRequest;
import de.leancoders.sharepoint.response.SharepointDriveItemResponse;
import de.leancoders.sharepoint.response.SharepointDriveItemsResponse;
import de.leancoders.sharepoint.response.SharepointDrivesResponse;
import de.leancoders.sharepoint.response.SharepointFields;
import de.leancoders.sharepoint.response.SharepointLists;
import de.leancoders.sharepoint.response.SharepointPermissionsResponse;
import de.leancoders.sharepoint.response.SharepointSiteResponse;
import de.leancoders.sharepoint.response.SharepointSitesResponse;
import io.restassured.http.ContentType;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Streams.stream;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class SharepointDriveClientService extends SharepointBaseClientService implements SharepointPaths {

    public SharepointDriveClientService(final SharepointConfig sharepointConfig,
                                        final SharepointClientService clientService) {
        super(sharepointConfig, clientService);
    }

    @Nonnull
    public SharepointSiteResponse sitesRoot() {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/sites/root/")
            .as(SharepointSiteResponse.class);
    }

    @Nonnull
    public SharepointSitesResponse sites() {
        return sites(100);
    }

    /**
     *
     * @param size the amount to be fetched
     * @return the sites
     */
    @Nonnull
    public SharepointSitesResponse sites(final int size) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .queryParam("$top", size)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/sites/")
            .as(SharepointSitesResponse.class);
    }

    @Nonnull
    public SharepointDrivesResponse drives(@NonNull final String siteId) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/sites/{siteId}/drives/", siteId)
            .as(SharepointDrivesResponse.class);
    }

    @Nonnull
    public SharepointDriveItemResponse rootDriveItem(@NonNull final String driveId) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/drives/{driveId}/root/", driveId)
            .as(SharepointDriveItemResponse.class);
    }

    @Nonnull
    public SharepointDriveItemsResponse rootDriveItemChildren(@NonNull final String driveId) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/drives/{driveId}/root/children/", driveId)
            .as(SharepointDriveItemsResponse.class);
    }

    @Nonnull
    public SharepointDriveItemsResponse driveItemChildren(@NonNull final String driveId,
                                                          @NonNull final String itemId) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/drives/{driveId}/items/{itemId}/children/", driveId, itemId)
            .as(SharepointDriveItemsResponse.class);
    }

    @Nonnull
    public SharepointDriveItemResponse driveItemListItems(@NonNull final String driveId,
                                                          @NonNull final String itemId) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/drives/{driveId}/items/{itemId}/listitem/", driveId, itemId)
            .as(SharepointDriveItemResponse.class);
    }

    @Nonnull
    public SharepointDriveItemResponse driveItemById(@NonNull final String driveId,
                                                     @NonNull final String itemId) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/drives/{driveId}/items/{itemId}/", driveId, itemId)
            .as(SharepointDriveItemResponse.class);
    }

    @Nonnull
    public SharepointDriveItemResponse driveItemByPath(@NonNull final String driveId,
                                                       @NonNull final Iterable<String> path) {

        final String fullPathString = String.join("/", path);

        return authContext()
            .authorizedRequest()
            .urlEncodingEnabled(false)
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/drives/{driveId}/items/root:/{path}:/", driveId, fullPathString)
            .as(SharepointDriveItemResponse.class);
    }


    @Nonnull
    public SharepointFields updateListItem(@NonNull final String siteId,
                                           @NonNull final String listId,
                                           @NonNull final String itemId,
                                           @NonNull final SharepointFields values) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .body(values)
            .expect().statusCode(200)
            .log().all()
            .when()
            .patch("v1.0/sites/{siteId}/lists/{listId}/items/{itemId}/fields/", siteId, listId, itemId)
            .as(SharepointFields.class);
    }

    @Nonnull
    public SharepointLists lists(@NonNull final String siteId) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/sites/{siteId}/lists/", siteId)
            .as(SharepointLists.class);
    }

    @Nonnull
    public SharepointDriveItemResponse createRootFolder(@NonNull final String driveId,
                                                        @NonNull final String name) {

        final SharepointFolderRequest request = new SharepointFolderRequest();
        request.setName(name);
        request.setConflictBehavior("fail");
        request.setFolder(new SharepointFolderRequest.SharepointFolderUpdateItem());

        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(request)
            .expect().statusCode(anyOf(is(200), is(201)))
            .log().all()
            .when()
            .post("v1.0/drives/{driveId}/items/root/children/", driveId)
            .as(SharepointDriveItemResponse.class);
    }

    @Nonnull
    public SharepointDriveItemResponse createFolder(@NonNull final String driveId,
                                                    @NonNull final Iterable<String> fullPath,
                                                    @NonNull final String name) {
        checkArgument(!isEmpty(fullPath), "fullPath must not be empty");

        final SharepointFolderRequest request = new SharepointFolderRequest();
        request.setName(name);
        // @microsoft.graph.conflictBehavior
        // fail, rename, replace
        // https://learn.microsoft.com/en-us/dynamics365/business-central/application/system-application/enum/system.integration.graph.graph-conflictbehavior
        request.setConflictBehavior("fail");
        request.setFolder(new SharepointFolderRequest.SharepointFolderUpdateItem());

        final String fullPathString = String.join("/", fullPath) + "/";

        return authContext()
            .authorizedRequest()
            .urlEncodingEnabled(false)
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(request)
            .expect().statusCode(anyOf(is(200), is(201), is(409)))
            .log().all()
            .when()
            .post("v1.0/drives/{driveId}/items/root:/{path}:/children/", driveId, fullPathString)
            .as(SharepointDriveItemResponse.class);
    }

    @Nonnull
    public SharepointDriveItemResponse createFile(@NonNull final String driveId,
                                                  @NonNull final Iterable<String> fullPath,
                                                  @NonNull final String contentType,
                                                  final byte[] buffer) {

        final String fullPathString = String.join("/", fullPath);

        return authContext()
            .authorizedRequest()
            .urlEncodingEnabled(false)
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .contentType(contentType)
            .body(buffer)
            .expect().statusCode(anyOf(is(200), is(201)))
            .log().all()
            .when()
            .put("v1.0/drives/{driveId}/root:/{path}:/content/", driveId, fullPathString)
            .as(SharepointDriveItemResponse.class);
    }

    @Nonnull
    public SharepointPermissionsResponse permissions(@NonNull final String driveId,
                                                     @NonNull final String itemId) {

        return authContext()
            .authorizedRequest()
            .urlEncodingEnabled(false)
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(anyOf(is(200), is(201)))
            .log().all()
            .when()
            .get("v1.0/drives/{drive-id}/items/{item-id}/permissions", driveId, itemId)
            .as(SharepointPermissionsResponse.class);
    }

    /**
     * Delete a permission on a drive item by DELETEing a fully formed {@link SharepointSiteGroupPermissionRequest} body.
     *
     * @see <a href="DELETE /drives/{drive-id}/items/{item-id}/permissions/{perm-id}">Delete Permission permission</a>
     */
    @Nonnull
    public String deletePermission(@NonNull final String driveId,
                                   @NonNull final String itemId,
                                   @NonNull final String permissionId) {

        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .contentType(ContentType.JSON)
            .expect().statusCode(anyOf(is(200), is(201), is(204)))
            .log().all()
            .when()
            .delete("v1.0/drives/{driveId}/items/{itemId}/permissions/{permissionId}", driveId, itemId, permissionId)
            .asString();
    }

    @Nonnull
    public SharepointPermissionsResponse invite(@NonNull final String driveId,
                                                @NonNull final String itemId,
                                                @NonNull final SharepointInviteRequest request) {

        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            // .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .body(request)
            .expect().statusCode(anyOf(is(200), is(201)))
            .log().all()
            .when()
            .post("v1.0/drives/{drive-id}/items/{item-id}/invite", driveId, itemId)
            .as(SharepointPermissionsResponse.class);
    }

    /**
     * Assigns a SharePoint (site) group - identified by its {@code principalId} and title - one or more roles on a
     * drive item (folder or file).
     *
     * <p>Only valid for items inside a <b>SharePoint Embedded container</b>; on a regular SharePoint Online document
     * library the underlying endpoint only accepts application permissions and this call will fail. The {@code itemId}
     * must be a child folder or file, never the container root.
     *
     * @param driveId           the drive (container) id
     * @param itemId            the child folder/file id
     * @param sharePointGroupId the sharepoint group's id (unique within the site)
     * @param roles             the roles to grant (e.g. {@link SharepointDriveItemRole#READ}, {@link SharepointDriveItemRole#WRITE})
     */
    @Nonnull
    public SharepointPermissionsResponse invite(@NonNull final String driveId,
                                                @NonNull final String itemId,
                                                @NonNull final String sharePointGroupId,
                                                @NonNull final Iterable<SharepointDriveItemRole> roles) {
        checkArgument(!isEmpty(roles), "at least one role must be provided");

        final List<String> roleNames =
            stream(roles)
                .map(SharepointDriveItemRole::getValue)
                .distinct()
                .collect(Collectors.toList())
            ;

        final SharepointInviteRequest request = new SharepointInviteRequest();
        request.setRequireSignIn(true);
        request.setSendInvitation(false);
        request.setMessage("Invitation to join SharePoint group");
        final SharepointInviteRequest.Recipient recipientGroup = new SharepointInviteRequest.Recipient();
        recipientGroup.setObjectId(sharePointGroupId);
        request.setRecipients(
            ImmutableList.of(
                recipientGroup
            )
        );

        request.setRoles(roleNames);

        return invite(driveId, itemId, request);
    }
}

