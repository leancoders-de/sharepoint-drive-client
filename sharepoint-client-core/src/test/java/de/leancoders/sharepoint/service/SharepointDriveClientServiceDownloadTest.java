package de.leancoders.sharepoint.service;

import com.google.common.collect.ImmutableList;
import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.model.SharepointTokenResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * Runs against a local MockServer standing in for both Graph and the SharePoint download host - no tenant needed.
 */
class SharepointDriveClientServiceDownloadTest {

    private static final String TOKEN = "graph-token";
    private static final String DOWNLOAD_PATH = "/_layouts/15/download.aspx";
    /**
     * Pre-authenticated urls arrive already encoded - a second encoding pass would turn {@code %3D} into {@code %253D}.
     */
    private static final String TEMPAUTH = "v1.eyJhbGciOiJub25lIn0%3D";
    /**
     * Not valid UTF-8, so any detour through a String would show.
     */
    private static final byte[] CONTENT = {0x25, 0x50, 0x44, 0x46, 0x00, (byte) 0xFF, (byte) 0xFE, 0x0A};

    private ClientAndServer mockServer;
    private SharepointDriveClientService service;

    @BeforeEach
    void setUp() {
        mockServer = ClientAndServer.startClientAndServer(0);

        final String uri = "http://localhost";
        final int port = mockServer.getPort();
        final SharepointConfig config = SharepointConfig.of(
            uri, port, uri, port, "client", "secret", "tenant", Path.of("unused.key"), Path.of("unused.crt"));

        final SharepointTokenResponse token = new SharepointTokenResponse();
        token.setAccessToken(TOKEN);
        token.setExpiresInSeconds(3600);

        final SharepointAuthService authService = resourceRoot ->
            SharepointAuthContext.success("tenant", "client", "secret", token, RestAssured::given);

        service = new SharepointDriveClientService(config, authService);

        mockServer
            .when(request().withMethod("GET").withPath(DOWNLOAD_PATH))
            .respond(response().withStatusCode(200).withHeader("Content-Type", "application/pdf").withBody(CONTENT));
    }

    @AfterEach
    void tearDown() {
        mockServer.stop();
    }

    @Test
    void downloadFileFollowsRedirectWithoutBearerToken() {
        redirect("/v1.0/drives/drive-1/items/item-1/content");

        final byte[] content = service.downloadFile("drive-1", "item-1");

        assertThat(content).isEqualTo(CONTENT);
        assertThat(single("/v1.0/drives/drive-1/items/item-1/content").getFirstHeader("Authorization"))
            .isEqualTo("Bearer " + TOKEN);

        final HttpRequest download = single(DOWNLOAD_PATH);
        assertThat(download.containsHeader("Authorization")).isFalse();
        assertThat(download.getFirstQueryStringParameter("tempauth")).isEqualTo("v1.eyJhbGciOiJub25lIn0=");
    }

    @Test
    void downloadFileFailsWhenItemIsMissing() {
        mockServer
            .when(request().withMethod("GET").withPath("/v1.0/drives/drive-1/items/missing/content"))
            .respond(response().withStatusCode(404));

        assertThatThrownBy(() -> service.downloadFile("drive-1", "missing"))
            .isInstanceOf(AssertionError.class);
        assertThat(mockServer.retrieveRecordedRequests(request().withPath(DOWNLOAD_PATH))).isEmpty();
    }

    private void redirect(final String graphPath) {
        final String location =
            "http://localhost:%d%s?UniqueId=abc&tempauth=%s".formatted(mockServer.getPort(), DOWNLOAD_PATH, TEMPAUTH);

        mockServer
            .when(request().withMethod("GET").withPath(graphPath))
            .respond(response().withStatusCode(302).withHeader("Location", location));
    }

    private HttpRequest single(final String path) {
        final HttpRequest[] recorded = mockServer.retrieveRecordedRequests(request().withPath(path));
        assertThat(recorded).hasSize(1);
        return recorded[0];
    }
}
