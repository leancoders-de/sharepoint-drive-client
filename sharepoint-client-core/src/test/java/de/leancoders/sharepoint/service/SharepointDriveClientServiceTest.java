package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.response.SharepointDriveItemsResponse;
import de.leancoders.sharepoint.response.SharepointDrivesResponse;
import de.leancoders.sharepoint.response.SharepointSiteResponse;
import de.leancoders.sharepoint.response.SharepointSitesResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

class SharepointDriveClientServiceTest {

    private SharepointConfig config;

    @BeforeEach
    void setUp() throws IOException {
        final Properties props = new Properties();
        props.load(Files.newInputStream(Paths.get("../prod.env")));
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
        final SharepointDriveClientService sharepointDriveClientService = new SharepointDriveClientService(config, new SharepointClientService(config));

        final SharepointSitesResponse sites = sharepointDriveClientService.sites(1000);
        System.out.println("sites = " + sites);
        final Iterable<String> siteNames = sites.getValue()
            .stream()
            .map(SharepointSiteResponse::getDisplayName)
            .filter(StringUtils::isNotBlank)
            .sorted()
            .collect(Collectors.toList())
            ;
        siteNames.forEach(System.out::println);

        // final SharepointDrivesResponse drives = sharepointDriveClientService.drives(siteId);
        // System.out.println("drives = " + drives);
        // final SharepointLists lists = sharepointDriveClientService.lists(siteId);
        // System.out.println("lists = " + lists);


        final String siteId = "qfmeu.sharepoint.com,8e4c9bff-27d4-495d-93bf-1d46fef71e6d,b3eea8db-7789-452b-a251-b9e107109bf6";
        final String driveId = "b!_5tMjtQnXUmTvx1G_vcebduo7rOJdytFolG54QcQm_YYgZ8ly82uS7Ekc1WWhhWA";
        final SharepointDrivesResponse drives = sharepointDriveClientService.drives(siteId);
        System.out.println("drives: " + drives);

        final List<String> folderStructure = Lists.newArrayList();

        final SharepointDriveItemsResponse sharepointDriveItemsResponse = sharepointDriveClientService.rootDriveItemChildren(driveId);
        sharepointDriveItemsResponse.getValue().forEach(item -> {
            final String id = item.getId();
            final String name = item.getName();
            final String webUrl = item.getWebUrl();
            folderStructure.add("Paths: %s / %s / %s".formatted(id, name, webUrl));


            final SharepointDriveItemsResponse children = sharepointDriveClientService.driveItemChildren(driveId, id);
            children.getValue().forEach(child -> {
                final String childId = child.getId();
                final String childName = child.getName();
                final String childWebUrl = child.getWebUrl();
                folderStructure.add(" Child Paths: %s / %s / %s".formatted(childId, childName, childWebUrl));
            });
        });

        folderStructure.forEach(System.out::println);

    }

    @Test
    void pureAuthSites() {
        final String token = "eyJ0eXAiOiJKV1QiLCJub25jZSI6IjNsclBTZ0xSaFZjTGZvTFBLMW1tUDFqOVN2YUJGUEs2TUJaQ0lsQjYwa3ciLCJhbGciOiJSUzI1NiIsIng1dCI6IkpZaEFjVFBNWl9MWDZEQmxPV1E3SG4wTmVYRSIsImtpZCI6IkpZaEFjVFBNWl9MWDZEQmxPV1E3SG4wTmVYRSJ9.eyJhdWQiOiJodHRwczovL2dyYXBoLm1pY3Jvc29mdC5jb20iLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8yMDMxODNiZC0yNDYzLTRjZTMtODhlNS0xZTMxMWViNDc1ZjkvIiwiaWF0IjoxNzU3MzIxODAwLCJuYmYiOjE3NTczMjE4MDAsImV4cCI6MTc1NzMyNTcwMCwiYWlvIjoiazJSZ1lKRDZraEU1cjZWVXNIU2hUTkNQWGRlbEFBPT0iLCJhcHBfZGlzcGxheW5hbWUiOiJtcy1ncmFwaC1zaGFyZXBvaW50IiwiYXBwaWQiOiIzZjAzM2ZmYS04NzY1LTQ4MTMtYjNlMC1kMDUwMDRhNzQ1YmIiLCJhcHBpZGFjciI6IjEiLCJpZHAiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC8yMDMxODNiZC0yNDYzLTRjZTMtODhlNS0xZTMxMWViNDc1ZjkvIiwiaWR0eXAiOiJhcHAiLCJvaWQiOiI1YTcwMDVmZS01YWI1LTRjZTAtOGM3Ny05ZDdkNDBlZGUzODEiLCJyaCI6IjEuQVhvQXZZTXhJR01rNDB5STVSNHhIclIxLVFNQUFBQUFBQUFBd0FBQUFBQUFBQURtQUFCNkFBLiIsInJvbGVzIjpbIkZpbGVJbmdlc3Rpb24uSW5nZXN0IiwiVXNlci5SZWFkQmFzaWMuQWxsIiwiRmlsZXMuUmVhZFdyaXRlLkFwcEZvbGRlciIsIlNpdGVzLlNlbGVjdGVkIiwiU2l0ZXMuUmVhZC5BbGwiLCJTaXRlcy5SZWFkV3JpdGUuQWxsIiwiU2l0ZXMuTWFuYWdlLkFsbCIsIkZpbGVzLlJlYWRXcml0ZS5BbGwiLCJTaXRlcy5BcmNoaXZlLkFsbCIsIlVzZXIuUmVhZC5BbGwiLCJGaWxlcy5SZWFkLkFsbCIsIkZpbGVTdG9yYWdlQ29udGFpbmVyLlNlbGVjdGVkIiwiU2l0ZXMuRnVsbENvbnRyb2wuQWxsIl0sInN1YiI6IjVhNzAwNWZlLTVhYjUtNGNlMC04Yzc3LTlkN2Q0MGVkZTM4MSIsInRlbmFudF9yZWdpb25fc2NvcGUiOiJFVSIsInRpZCI6IjIwMzE4M2JkLTI0NjMtNGNlMy04OGU1LTFlMzExZWI0NzVmOSIsInV0aSI6IjNULWhHNEZ5eDBHQVdvRFlsTzVSQUEiLCJ2ZXIiOiIxLjAiLCJ3aWRzIjpbIjA5OTdhMWQwLTBkMWQtNGFjYi1iNDA4LWQ1Y2E3MzEyMWU5MCJdLCJ4bXNfZnRkIjoiLUZpbFRPdjdObUdrYmhvTmtycUdRUG1wSzI5eVgyU0h4ODFHb1BrQ21HVUJaWFZ5YjNCbGQyVnpkQzFrYzIxeiIsInhtc19pZHJlbCI6IjcgMjAiLCJ4bXNfcmQiOiIwLjQyTGxZQkppckJJUzRXQVhFZ2hMbGZyMjl0RTgxNzQxcmZ6TEhONlhBVVU1aFFUT2Jmd2pjMGJtbldNWHAyN0lIcDJ6NVVCUkRpRUJaZ1lJT0FDbEFRIiwieG1zX3RjZHQiOjE2MDU1MTY1NDksInhtc190ZGJyIjoiRVUifQ.XejCuxHzVhhgVDJtD8MDOuTAwoxC6aeTAscSRuuviyo4BkORhSIs9KUr8T5M2y6cUSfbUD0mnggECv72ZKx2vb06Cmm95aSK-rmHT3AP--vEj-tFMrWpu4yGGRS9YkKQx9rwp1IHeY9AhA9900kLGc_MyZ3R6fNOojAg_cVX5_A1ArXEPJAj__7e9IbPSKfwXt6tOGT2pToq3iQQeag5DUjx_Z2OSeU7AGgwDdsiL8aoZEbaWBjqxBHlQoUSfKfrCrR255Yeeqq3JTWo23L6FkDcuUaVQt0nGUkzWOonrlcc9MBanCt6Q-lb_vS8CgEboyvOEMks4JMemikX3sJrUQ";
        final SharepointSitesResponse sites =
            RestAssured.given()
                .urlEncodingEnabled(false)
                .port(443)
                .baseUri("https://graph.microsoft.com/")
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .log().all()
                .accept(ContentType.JSON)
                .expect().statusCode(200)
                .log().all()
                .when()
                .get("v1.0/sites/")
                .as(SharepointSitesResponse.class)
            ;

        System.out.println("sites = " + sites);
    }

}