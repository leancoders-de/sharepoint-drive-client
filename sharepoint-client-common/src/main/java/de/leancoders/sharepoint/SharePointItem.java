package de.leancoders.sharepoint;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor(staticName = "of")
@Data
public class SharePointItem {

    private String sharepointDriveId;
    private String sharepointItemId;
    private String name;
    private boolean folder;
    private int childrenCount;
    private Iterable<String> parents;

}
