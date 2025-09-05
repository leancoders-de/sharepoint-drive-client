package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public abstract class SharepointListResponse<T> {

    @JsonProperty("@odata.context")
    private String context;
    @JsonProperty("value")
    private List<T> value;


}
