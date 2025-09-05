package de.leancoders.sharepoint.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointSiteCollection {

    @JsonProperty("hostname")
    private String hostname;

}
