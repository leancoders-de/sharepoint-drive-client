package de.leancoders.sharepoint.service;

import de.leancoders.sharepoint.model.SharepointConfig;
import de.leancoders.sharepoint.model.SharepointTokenResponse;
import de.leancoders.sharepoint.request.SharepointConflictBehavior;
import de.leancoders.sharepoint.request.SharepointCopyRequest;
import de.leancoders.sharepoint.response.SharepointAsyncJobStatus;
import de.leancoders.sharepoint.response.SharepointDriveItemResponse;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;

import java.nio.file.Path;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

/**
 * Runs against a local MockServer standing in for both Graph and the SharePoint monitor host - no tenant needed.
 */
class SharepointDriveClientServiceCopyTest {

    private static final String TOKEN = "graph-token";
    private static final String COPY_PATH = "/v1.0/drives/from-drive/items/from-item/copy";
    private static final String MONITOR_PATH = "/_api/v2.0/monitor/4A3407B5-88FC-4504-8B21-0AABD3412717";
    private static final String NEW_ITEM_ID = "016OGUCSF6Y2GOVW7725BZO354PWSELRRZ";

    private static final String IN_PROGRESS = """
        {
          "operation": "ItemCopy",
          "percentageComplete": 27.8,
          "status": "inProgress"
        }
        """;

    private static final String COMPLETED = """
        {
          "id": "049af13f-d177-4c70-aed0-eb6f04a5d88b",
          "createdDateTime": "0001-01-01T00:00:00Z",
          "lastActionDateTime": "0001-01-01T00:00:00Z",
          "percentageComplete": 100,
          "percentComplete": 100,
          "resourceId": "%s",
          "status": "completed"
        }
        """.formatted(NEW_ITEM_ID);

    private static final String FAILED = """
        {
          "id": "46cf980a-28e1-4623-b8d0-11fc5278efe6",
          "status": "failed",
          "error": {
            "code": "nameAlreadyExists",
            "message": "Name already exists"
          }
        }
        """;

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
            .when(request().withMethod("POST").withPath(COPY_PATH))
            .respond(response().withStatusCode(202).withHeader("Location", monitorUrl()));
    }

    @AfterEach
    void tearDown() {
        mockServer.stop();
    }

    @Test
    void copyPostsParentReferenceAndReturnsMonitorUrl() {
        final String monitorUrl = service.copy(
            "from-drive", "from-item", "to-drive", "to-folder", "plan (copy).txt", SharepointConflictBehavior.RENAME);

        assertThat(monitorUrl).isEqualTo(monitorUrl());

        final HttpRequest copy = single(COPY_PATH);
        assertThat(copy.getFirstHeader("Authorization")).isEqualTo("Bearer " + TOKEN);
        assertThat(copy.getFirstQueryStringParameter("@microsoft.graph.conflictBehavior")).isEqualTo("rename");
        assertThat(compact(copy))
            .contains("\"driveId\":\"to-drive\"")
            .contains("\"id\":\"to-folder\"")
            .contains("\"name\":\"plan(copy).txt\"");
    }

    /**
     * The unset flags have to stay out of the payload: {@code name} alongside {@code childrenOnly} is rejected, and
     * Graph ignores {@code includeAllVersionHistory} whenever a {@code name} is sent.
     */
    @Test
    void copyOmitsPropertiesThatWereNotSet() {
        service.copy("from-drive", "from-item", "to-drive", "to-folder", SharepointConflictBehavior.FAIL);

        assertThat(compact(single(COPY_PATH)))
            .doesNotContain("name")
            .doesNotContain("childrenOnly")
            .doesNotContain("includeAllVersionHistory");
    }

    @Test
    void copyPassesChildrenOnlyAndVersionHistory() {
        final SharepointCopyRequest.SharepointItemReference parentReference =
            new SharepointCopyRequest.SharepointItemReference();
        parentReference.setDriveId("to-drive");
        parentReference.setId("to-folder");

        final SharepointCopyRequest request = new SharepointCopyRequest();
        request.setParentReference(parentReference);
        request.setChildrenOnly(true);
        request.setIncludeAllVersionHistory(true);

        service.copy("from-drive", "from-item", request, SharepointConflictBehavior.REPLACE);

        assertThat(compact(single(COPY_PATH)))
            .contains("\"childrenOnly\":true")
            .contains("\"includeAllVersionHistory\":true");
    }

    @Test
    void copyStatusReadsMonitorUrlWithoutBearerToken() {
        monitor(200, COMPLETED);

        final SharepointAsyncJobStatus status = service.copyStatus(monitorUrl());

        assertThat(status.isCompleted()).isTrue();
        assertThat(status.isDone()).isTrue();
        assertThat(status.getResourceId()).isEqualTo(NEW_ITEM_ID);
        assertThat(status.getPercentageComplete()).isEqualTo(100.0);
        assertThat(single(MONITOR_PATH).containsHeader("Authorization")).isFalse();
    }

    @Test
    void copyStatusAcceptsTheAcceptedOfAJobStillRunning() {
        monitor(202, IN_PROGRESS);

        final SharepointAsyncJobStatus status = service.copyStatus(monitorUrl());

        assertThat(status.isDone()).isFalse();
        assertThat(status.getOperation()).isEqualTo("ItemCopy");
        assertThat(status.getPercentageComplete()).isEqualTo(27.8);
    }

    @Test
    void awaitCopyPollsUntilTheJobIsDone() {
        mockServer
            .when(request().withMethod("GET").withPath(MONITOR_PATH), Times.exactly(1))
            .respond(response().withStatusCode(202).withBody(json(IN_PROGRESS)));
        monitor(200, COMPLETED);

        final SharepointAsyncJobStatus status = service.awaitCopy(monitorUrl(), Duration.ofSeconds(30));

        assertThat(status.isCompleted()).isTrue();
        assertThat(mockServer.retrieveRecordedRequests(request().withPath(MONITOR_PATH))).hasSize(2);
    }

    @Test
    void awaitCopyStopsAtAFailedJob() {
        monitor(200, FAILED);

        final SharepointAsyncJobStatus status = service.awaitCopy(monitorUrl(), Duration.ofSeconds(30));

        assertThat(status.isFailed()).isTrue();
        assertThat(status.getError().getCode()).isEqualTo("nameAlreadyExists");
    }

    @Test
    void awaitCopyGivesUpAfterTheTimeout() {
        monitor(202, IN_PROGRESS);

        assertThatThrownBy(() -> service.awaitCopy(monitorUrl(), Duration.ofNanos(1)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("inProgress");
    }

    @Test
    void copyAndAwaitReadsTheNewItemFromTheTargetDrive() {
        monitor(200, COMPLETED);
        mockServer
            .when(request().withMethod("GET").withPath("/v1.0/drives/to-drive/items/" + NEW_ITEM_ID + "/"))
            .respond(response()
                .withStatusCode(200)
                .withBody(json("{\"id\":\"%s\",\"name\":\"plan (copy).txt\"}".formatted(NEW_ITEM_ID))));

        final SharepointDriveItemResponse item = service.copyAndAwait(
            "from-drive", "from-item", "to-drive", "to-folder", "plan (copy).txt",
            SharepointConflictBehavior.RENAME, Duration.ofSeconds(30));

        assertThat(item.getId()).isEqualTo(NEW_ITEM_ID);
        assertThat(item.getName()).isEqualTo("plan (copy).txt");
    }

    @Test
    void copyAndAwaitFailsOnAFailedJob() {
        monitor(200, FAILED);

        assertThatThrownBy(() -> service.copyAndAwait(
            "from-drive", "from-item", "to-drive", "to-folder", null,
            SharepointConflictBehavior.FAIL, Duration.ofSeconds(30)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("nameAlreadyExists");
    }

    private String monitorUrl() {
        return "http://localhost:%d%s".formatted(mockServer.getPort(), MONITOR_PATH);
    }

    private void monitor(final int statusCode, final String body) {
        mockServer
            .when(request().withMethod("GET").withPath(MONITOR_PATH))
            .respond(response().withStatusCode(statusCode).withBody(json(body)));
    }

    /**
     * MockServer hands recorded json bodies back pretty printed, so whitespace is dropped before matching.
     */
    private String compact(final HttpRequest recorded) {
        return recorded.getBodyAsJsonOrXmlString().replaceAll("\\s", "");
    }

    private HttpRequest single(final String path) {
        final HttpRequest[] recorded = mockServer.retrieveRecordedRequests(request().withPath(path));
        assertThat(recorded).hasSize(1);
        return recorded[0];
    }
}
