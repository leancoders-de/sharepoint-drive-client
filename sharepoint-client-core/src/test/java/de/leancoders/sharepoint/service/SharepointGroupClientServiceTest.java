package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.response.SharepointGroupsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

class SharepointGroupClientServiceTest {

    private SharepointConfig config;

    @BeforeEach
    void setUp() throws IOException {
        final Properties props = new Properties();
        props.load(Files.newInputStream(Paths.get("../stage.env")));
        config = SharepointConfig.of(
            props.getProperty("SHAREPOINT_AUTH_URI"),
            Integer.parseInt(props.getProperty("SHAREPOINT_AUTH_PORT")),
            props.getProperty("SHAREPOINT_GRAPH_URI"),
            Integer.parseInt(props.getProperty("SHAREPOINT_GRAPH_PORT")),
            props.getProperty("SHAREPOINT_APP_CLIENT_ID"),
            props.getProperty("SHAREPOINT_APP_CLIENT_SECRET"),
            props.getProperty("SHAREPOINT_APP_TENANT_ID")
        );
    }

    @Test
    void createRootFolder() {
        final SharepointGroupsClientService clientService = new SharepointGroupsClientService(config, new SharepointClientService(config));

        final SharepointGroupsResponse groups = clientService.groups();
        System.out.println("groups = " + groups);


        groups.getValue().forEach(group -> {
            System.out.println("group = " + group);
        });

    }


}