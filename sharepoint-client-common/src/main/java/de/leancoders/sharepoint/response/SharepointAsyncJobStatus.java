package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Progress report of a long running action, read from the monitor url that the {@code 202 Accepted} of
 * {@code /copy} hands out in its {@code Location} header.
 *
 * <p>Graph documents {@code percentageComplete}, SharePoint answers with both spellings, and neither is present
 * once the job has failed - only {@link #status} and {@link #error} can be relied on.
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/resources/asyncjobstatus">asyncJobStatus</a>
 * @see <a href="https://learn.microsoft.com/en-us/graph/long-running-actions-overview">Long running actions</a>
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SharepointAsyncJobStatus {

    public static final String STATUS_NOT_STARTED = "notStarted";
    public static final String STATUS_IN_PROGRESS = "inProgress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_UPDATING = "updating";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_DELETE_PENDING = "deletePending";
    public static final String STATUS_DELETE_FAILED = "deleteFailed";
    public static final String STATUS_WAITING = "waiting";

    @JsonProperty("@odata.context")
    private String oDataContext;

    @JsonProperty("id")
    private String id;

    @JsonProperty("createdDateTime")
    private LocalDateTime createdDateTime;

    @JsonProperty("lastActionDateTime")
    private LocalDateTime lastActionDateTime;

    /**
     * The kind of action being reported on, e.g. {@code ItemCopy}. Not always populated.
     */
    @JsonProperty("operation")
    private String operation;

    @JsonProperty("percentageComplete")
    private Double percentageComplete;

    /**
     * SharePoint's spelling of {@link #percentageComplete}; it sends both.
     */
    @JsonProperty("percentComplete")
    private Double percentComplete;

    /**
     * The id of the new drive item, once {@link #status} is {@value #STATUS_COMPLETED}. It lives in the
     * <i>target</i> drive, not in the source drive.
     */
    @JsonProperty("resourceId")
    private String resourceId;

    @JsonProperty("resourceLocation")
    private String resourceLocation;

    @JsonProperty("status")
    private String status;

    /**
     * Only set on a failed job. A {@code childrenOnly} copy reports one entry per child in
     * {@link SharepointAsyncJobError#details}.
     */
    @JsonProperty("error")
    private SharepointAsyncJobError error;

    @JsonIgnore
    public boolean isCompleted() {
        return STATUS_COMPLETED.equalsIgnoreCase(status);
    }

    @JsonIgnore
    public boolean isFailed() {
        return STATUS_FAILED.equalsIgnoreCase(status)
            || STATUS_DELETE_FAILED.equalsIgnoreCase(status);
    }

    /**
     * Whether the job has reached a terminal state and polling the monitor url can stop.
     */
    @JsonIgnore
    public boolean isDone() {
        return isCompleted() || isFailed();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SharepointAsyncJobError {

        /**
         * Graph error code, e.g. {@code nameAlreadyExists} or {@code invalidRequest}. Absent on the umbrella
         * error of a partially failed {@code childrenOnly} copy, where only the {@link #details} carry codes.
         */
        @JsonProperty("code")
        private String code;

        @JsonProperty("message")
        private String message;

        /**
         * The item the error refers to, e.g. the id of the conflicting child.
         */
        @JsonProperty("target")
        private String target;

        @JsonProperty("details")
        private List<SharepointAsyncJobError> details;
    }
}
