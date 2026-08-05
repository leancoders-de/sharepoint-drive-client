package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.response.SharepointGroupsResponse;
import io.restassured.http.ContentType;

import javax.annotation.Nonnull;

public class SharepointGroupsClientService extends SharepointBaseClientService implements SharepointPaths {

    public SharepointGroupsClientService(final SharepointConfig sharepointConfig,
                                         final SharepointAuthService clientService) {
        super(sharepointConfig, clientService);
    }

    @Nonnull
    public SharepointGroupsResponse groups() {
        return authContext()
            .authorizedRequest()
            .baseUri(config.getGraphUri())
            .port(config.getGraphPort())
            .log().all()
            .accept(ContentType.JSON)
            .expect().statusCode(200)
            .log().all()
            .when()
            .get("v1.0/groups/")
            .as(SharepointGroupsResponse.class);
    }

}

