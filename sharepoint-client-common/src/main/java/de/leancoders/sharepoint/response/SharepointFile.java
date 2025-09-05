package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SharepointFile {
    @JsonProperty("hashes")
    private Hashes hashes;

    @JsonProperty("mimeType")
    private String mimeType;

    @Data
    public static class Hashes {
        @JsonProperty("quickXorHash")
        private String quickXorHash;
    }
}