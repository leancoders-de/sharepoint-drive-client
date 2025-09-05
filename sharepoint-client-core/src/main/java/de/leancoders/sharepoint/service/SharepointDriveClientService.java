package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.request.SharepointFolderRequest;
import de.leancoders.sharepoint.response.SharepointDriveItemResponse;
import de.leancoders.sharepoint.response.SharepointDriveItemsResponse;
import de.leancoders.sharepoint.response.SharepointDrivesResponse;
import de.leancoders.sharepoint.response.SharepointFields;
import de.leancoders.sharepoint.response.SharepointLists;
import de.leancoders.sharepoint.response.SharepointSiteResponse;
import de.leancoders.sharepoint.response.SharepointSitesResponse;
import io.restassured.http.ContentType;
import lombok.NonNull;

import javax.annotation.Nonnull;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

public class SharepointDriveClientService extends SharepointBaseClientService implements SharepointPaths {

    public SharepointDriveClientService(final SharepointConfig sharepointConfig,
                                        final SharepointClientService clientService) {
        super(sharepointConfig, clientService);
    }

    // https://graph.microsoft.com/v1.0/sites/root
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
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/sites/")
            .as(SharepointSitesResponse.class);
    }

    // https://graph.microsoft.com/v1.0/sites/leancodersde.sharepoint.com,2a58be08-8d56-44ed-942b-6e892404f8fc,2bd44f8e-c688-4990-9c4a-6b331c928b12/drives
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

    // https://graph.microsoft.com/v1.0/drives/b!CL5YKlaN7USUK26JJAT4_I5P1CuIxpBJnEprMxySixLPksDhzr9FSJar5IgdR-xd/items/01NKOCY3FPF3HNSL6HIJCJVL4DYNOTA2CS/listItem/
    // https://graph.microsoft.com/v1.0/drives/b!CL5YKlaN7USUK26JJAT4_I5P1CuIxpBJnEprMxySixLPksDhzr9FSJar5IgdR-xd/root/
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
    }    // https://graph.microsoft.com/v1.0/drives/b!CL5YKlaN7USUK26JJAT4_I5P1CuIxpBJnEprMxySixLPksDhzr9FSJar5IgdR-xd/root/

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


    // PATCH https://graph.microsoft.com/v1.0/sites/{site-id}/lists/{list-id}/items/{item-id}/fields
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

    // POST https://graph.microsoft.com/v1.0/me/drive/root/children
    //Content-Type: application/json
    //
    //{
    //  "name": "New Folder",
    //  "folder": { },
    //  "@microsoft.graph.conflictBehavior": "rename"
    //}
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

    // POST /drives/{drive-id}/items/{parent-item-id}/children
    //Content-Type: application/json
    //
    //{
    //  "name": "New Folder",
    //  "folder": { },
    //  "@microsoft.graph.conflictBehavior": "rename"
    //}
    @Nonnull
    public SharepointDriveItemResponse createFolder(@NonNull final String driveId,
                                                    @NonNull final Iterable<String> fullPath,
                                                    @NonNull final String name) {
        final SharepointFolderRequest request = new SharepointFolderRequest();
        request.setName(name);
        // @microsoft.graph.conflictBehavior
        // fail, rename, replace
        // https://learn.microsoft.com/en-us/dynamics365/business-central/application/system-application/enum/system.integration.graph.graph-conflictbehavior
        request.setConflictBehavior("fail");
        request.setFolder(new SharepointFolderRequest.SharepointFolderUpdateItem());

        final String fullPathString = String.join("/", fullPath);

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


    // TODO create file
    //  PUT /drives/{drive-id}/items/{parent-id}:/{filename}:/content
    //  PUT /drives/{drive-id}/root:/FolderA/FileB.txt:/content
    //  PUT https://graph.microsoft.com/v1.0/drives/b!HQD0spl5J0SwS-xjLHvPUCkgbpkMZ4xFqbyoOaUj4Kl3PR79YHGISqnmuVmScbeI/root:/path/to/file/file.png:/content/

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
}

