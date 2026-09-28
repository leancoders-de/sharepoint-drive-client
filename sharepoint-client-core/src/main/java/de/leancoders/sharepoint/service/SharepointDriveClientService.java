package de.leancoders.sharepoint.service;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.request.SharepointConflictBehavior;
import de.leancoders.sharepoint.request.SharepointCopyRequest;
import de.leancoders.sharepoint.request.SharepointDriveItemRole;
import de.leancoders.sharepoint.request.SharepointFolderRequest;
import de.leancoders.sharepoint.request.SharepointInviteRequest;
import de.leancoders.sharepoint.request.SharepointSiteGroupPermissionRequest;
import de.leancoders.sharepoint.response.SharepointAsyncJobStatus;
import de.leancoders.sharepoint.response.SharepointDriveItemResponse;
import de.leancoders.sharepoint.response.SharepointDriveItemsResponse;
import de.leancoders.sharepoint.response.SharepointDrivesResponse;
import de.leancoders.sharepoint.response.SharepointFields;
import de.leancoders.sharepoint.response.SharepointLists;
import de.leancoders.sharepoint.response.SharepointPermissionResponse;
import de.leancoders.sharepoint.response.SharepointPermissionsResponse;
import de.leancoders.sharepoint.response.SharepointSiteResponse;
import de.leancoders.sharepoint.response.SharepointSitesResponse;
import io.restassured.http.ContentType;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Streams.stream;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class SharepointDriveClientService extends SharepointBaseClientService implements SharepointPaths {

    private static final Joiner SELECT_JOINER = Joiner.on(",").skipNulls();

    /**
     * Unlike the item creation calls, which take it as a body property, {@code /copy} expects the conflict
     * behavior as a query parameter.
     */
    private static final String GRAPH_CONFLICT_BEHAVIOR = "@microsoft.graph.conflictBehavior";

    /**
     * How long {@link #awaitCopy(String, Duration)} waits between two reads of a monitor url.
     */
    private static final Duration COPY_POLL_INTERVAL = Duration.ofSeconds(2);

    /**
     * Graph omits {@code sharepointIds} from the default driveItem payload, so it has to be selected explicitly.
     * Because {@code $select} is exclusive, every other property {@link de.leancoders.sharepoint.response.SharepointDriveItem}
     * maps has to be listed alongside it.
     *
     * <p>An {@link ImmutableSet} rather than a plain set - it keeps insertion order, so the query string stays stable.
     */
    private static final Iterable<String> DRIVE_ITEM_SELECT =
        ImmutableSet.of(
            "id",
            "name",
            "createdDateTime",
            "lastModifiedDateTime",
            "parentReference",
            "webUrl",
            "fileSystemInfo",
            "folder",
            "root",
            "size",
            "createdBy",
            "cTag",
            "eTag",
            "lastModifiedBy",
            "file",
            "shared",
            "contentType",
            "sharepointIds"
        );

    public SharepointDriveClientService(final SharepointConfig sharepointConfig,
                                        final SharepointAuthService clientService) {
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

    /**
     * Fetches a drive item including its {@code sharepointIds} facet, which carries the {@code listId},
     * {@code listItemId} and {@code siteUrl} needed to address the item through the classic SharePoint REST API
     * (e.g. for {@code breakroleinheritance} / {@code addroleassignment}).
     */
    @Nonnull
    public SharepointDriveItemResponse driveItemById(@NonNull final String driveId,
                                                     @NonNull final String itemId) {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .queryParam("$select", SELECT_JOINER.join(DRIVE_ITEM_SELECT))
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

    /**
     * Copies a file or folder into a folder of a - possibly different - drive, keeping the source name.
     *
     * @see #copy(String, String, SharepointCopyRequest, SharepointConflictBehavior)
     */
    @Nonnull
    public String copy(@NonNull final String fromDriveId,
                       @NonNull final String fromItemId,
                       @NonNull final String toDriveId,
                       @NonNull final String toFolderItemId,
                       @NonNull final SharepointConflictBehavior conflictBehavior) {

        return copy(fromDriveId, fromItemId, copyRequest(toDriveId, toFolderItemId, ""), conflictBehavior);
    }

    /**
     * Copies a file or folder into a folder of a - possibly different - drive under a new name.
     *
     * @see #copy(String, String, SharepointCopyRequest, SharepointConflictBehavior)
     */
    @Nonnull
    public String copy(@NonNull final String fromDriveId,
                       @NonNull final String fromItemId,
                       @NonNull final String toDriveId,
                       @NonNull final String toFolderItemId,
                       @NonNull final String newName,
                       @NonNull final SharepointConflictBehavior conflictBehavior) {

        return copy(fromDriveId, fromItemId, copyRequest(toDriveId, toFolderItemId, newName), conflictBehavior);
    }

    /**
     * Queues a copy of a drive item.
     *
     * <p>Copying is asynchronous: Graph only accepts the job and answers {@code 202 Accepted} with a monitor url in
     * its {@code Location} header. A successful return therefore says nothing about the copy itself - a name clash
     * in the target folder surfaces as a {@code nameAlreadyExists} on the monitor url, never here. Hand the url to
     * {@link #copyStatus(String)} or {@link #awaitCopy(String, Duration)}, or use
     * {@link #copyAndAwait(String, String, String, String, String, SharepointConflictBehavior, Duration)} to get the
     * new item in one call.
     *
     * <p>Neither metadata nor permissions are carried over - the copy inherits the target folder's permissions - and
     * only the latest major version is copied unless
     * {@link SharepointCopyRequest#setIncludeAllVersionHistory(Boolean)} says otherwise. A single job copies at most
     * 30.000 items.
     *
     * @param driveId          the drive the source item lives in
     * @param itemId           the file or folder to copy; a drive root only works together with
     *                         {@link SharepointCopyRequest#setChildrenOnly(Boolean)}
     * @param request          the target folder plus the optional name and copy flags
     * @param conflictBehavior how a name clash in the target folder is resolved
     * @return the monitor url the copy reports its progress on
     * @see <a href="https://learn.microsoft.com/en-us/graph/api/driveitem-copy?view=graph-rest-1.0">driveItem: copy</a>
     */
    @Nonnull
    public String copy(@NonNull final String driveId,
                       @NonNull final String itemId,
                       @NonNull final SharepointCopyRequest request,
                       @NonNull final SharepointConflictBehavior conflictBehavior) {

        final String monitorUrl =
            authContext()
                .authorizedRequest()
                // the query parameter name carries an '@' and dots that are meant to reach Graph unencoded
                .urlEncodingEnabled(false)
                .baseUri(config.getGraphUri())
                .port(config.getGraphPort())
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam(GRAPH_CONFLICT_BEHAVIOR, conflictBehavior.getValue())
                .body(request)
                .expect().statusCode(202)
                .log().all()
                .when()
                .post("v1.0/drives/{driveId}/items/{itemId}/copy/", driveId, itemId)
                .header(HttpHeaders.LOCATION)
            ;

        checkState(!isNullOrEmpty(monitorUrl), "graph accepted the copy without a monitor location");

        return monitorUrl;
    }

    /**
     * Reads the current progress of a queued copy from its monitor url.
     *
     * <p>The url is short-lived, unique to the original caller and - like the download url - served by SharePoint
     * rather than by Graph, so it is requested without the Graph bearer token.
     *
     * @param monitorUrl the url {@link #copy(String, String, SharepointCopyRequest, SharepointConflictBehavior)} returned
     * @see <a href="https://learn.microsoft.com/en-us/graph/long-running-actions-overview">Long running actions</a>
     */
    @Nonnull
    public SharepointAsyncJobStatus copyStatus(@NonNull final String monitorUrl) {
        checkArgument(!isNullOrEmpty(monitorUrl), "monitorUrl must not be empty");

        return authContext()
            .getRequestSpecification()
            .get()
            .urlEncodingEnabled(false)
            .accept(ContentType.JSON)
            .log().all()
            // a job still running answers 202, a finished one 200 - both carry an asyncJobStatus body
            .expect().statusCode(anyOf(is(200), is(202)))
            .log().all()
            .when()
            .get(monitorUrl)
            .as(SharepointAsyncJobStatus.class);
    }

    /**
     * Polls a monitor url until the copy has either completed or failed.
     *
     * @param monitorUrl the url {@link #copy(String, String, SharepointCopyRequest, SharepointConflictBehavior)} returned
     * @param timeout    how long to keep polling before giving up
     * @return the terminal status - check {@link SharepointAsyncJobStatus#isCompleted()}; a failed job carries its
     * reason in {@link SharepointAsyncJobStatus#getError()}
     * @throws IllegalStateException if the copy has not reached a terminal state within {@code timeout}
     */
    @Nonnull
    public SharepointAsyncJobStatus awaitCopy(@NonNull final String monitorUrl,
                                              @NonNull final Duration timeout) {
        checkArgument(timeout.compareTo(Duration.ZERO) > 0, "timeout must be positive");

        final Instant deadline = Instant.now().plus(timeout);

        SharepointAsyncJobStatus status = copyStatus(monitorUrl);
        while (!status.isDone()) {
            checkState(
                Instant.now().isBefore(deadline),
                "copy did not finish within %s, last reported status was %s", timeout, status.getStatus()
            );
            sleep(COPY_POLL_INTERVAL);
            status = copyStatus(monitorUrl);
        }

        return status;
    }

    /**
     * Copies a drive item, waits for the job to finish and fetches the new item.
     *
     * <p>The copy is created in {@code toDriveId}, so that is the drive the returned item is read from.
     *
     * @param newName the name of the copy, or {@code null} to keep the source name
     * @throws IllegalStateException if the copy failed or did not finish within {@code timeout}
     */
    @Nonnull
    public SharepointDriveItemResponse copyAndAwait(@NonNull final String fromDriveId,
                                                    @NonNull final String fromItemId,
                                                    @NonNull final String toDriveId,
                                                    @NonNull final String toFolderItemId,
                                                    @NonNull final String newName,
                                                    @NonNull final SharepointConflictBehavior conflictBehavior,
                                                    @NonNull final Duration timeout) {

        final SharepointCopyRequest sharepointCopyRequest = copyRequest(toDriveId, toFolderItemId, newName);
        final String monitorUrl = copy(fromDriveId, fromItemId, sharepointCopyRequest, conflictBehavior);

        final SharepointAsyncJobStatus status = awaitCopy(monitorUrl, timeout);
        checkState(status.isCompleted(), "copy failed: %s", status.getError());
        checkState(!isNullOrEmpty(status.getResourceId()), "copy completed without a resource id");

        return driveItemById(toDriveId, status.getResourceId());
    }

    @Nonnull
    private static SharepointCopyRequest copyRequest(@NonNull final String toDriveId,
                                                     @NonNull final String toFolderItemId,
                                                     @NonNull final String newName) {

        final SharepointCopyRequest.SharepointItemReference parentReference = new SharepointCopyRequest.SharepointItemReference();
        parentReference.setDriveId(toDriveId);
        parentReference.setId(toFolderItemId);

        final SharepointCopyRequest request = new SharepointCopyRequest();
        request.setParentReference(parentReference);
        request.setName(trimToNull(newName));

        return request;
    }

    /**
     * Downloads the content of a file.
     *
     * <p>The whole file is held in memory, so this is meant for documents rather than arbitrarily large files.
     */
    @Nonnull
    public byte[] downloadFile(@NonNull final String driveId,
                               @NonNull final String itemId) {

        final String downloadUrl =
            authContext()
                .authorizedRequest()
                .baseUri(config.getGraphUri())
                .port(config.getGraphPort())
                .redirects().follow(false)
                .log().all()
                .expect().statusCode(302)
                .log().all()
                .when()
                .get("v1.0/drives/{driveId}/items/{itemId}/content", driveId, itemId)
                .header(HttpHeaders.LOCATION)
            ;

        return download(downloadUrl);
    }

    /**
     * Graph answers {@code /content} with a {@code 302} to a short-lived, pre-authenticated url on the SharePoint
     * host. The redirect is followed by hand: RestAssured would replay every header - the Graph bearer token
     * included - against that host.
     *
     * <p>The response is only logged on error, so binary content does not end up in the log.
     */
    @Nonnull
    private byte[] download(final String downloadUrl) {
        checkState(!isNullOrEmpty(downloadUrl), "graph answered without a download location");

        return authContext()
            .getRequestSpecification()
            .get()
            .urlEncodingEnabled(false)
            .log().all()
            .expect().statusCode(200)
            .log().ifError()
            .when()
            .get(downloadUrl)
            .asByteArray();
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
    public SharepointPermissionResponse invite(@NonNull final String driveId,
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
            .as(SharepointPermissionResponse.class);
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
    public SharepointPermissionResponse invite(@NonNull final String driveId,
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

