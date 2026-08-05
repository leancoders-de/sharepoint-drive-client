package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * The {@code sharepointIds} facet of a driveItem - the bridge between Microsoft Graph ids and the classic
 * SharePoint REST API ({@code _api/web/...}).
 *
 * <p>Only returned when explicitly requested via {@code $select=sharepointIds}; it is not part of the default
 * driveItem payload.
 *
 * <p>{@link #listId} + {@link #listItemId} address the item for {@code breakroleinheritance} and
 * {@code addroleassignment}, {@link #siteUrl} is the base uri for those calls.
 *
 * @see <a href="https://learn.microsoft.com/en-us/graph/api/resources/sharepointids">sharePointIds resource type</a>
 */
@Data
public class SharepointIds {

    /**
     * The guid of the list (document library) containing the item.
     */
    @JsonProperty("listId")
    private String listId;

    /**
     * The integer id of the item within its list - note this is <b>not</b> the driveItem id.
     */
    @JsonProperty("listItemId")
    private String listItemId;

    /**
     * The guid that uniquely identifies the item across the site collection.
     */
    @JsonProperty("listItemUniqueId")
    private String listItemUniqueId;

    @JsonProperty("siteId")
    private String siteId;

    /**
     * The absolute url of the site, e.g. {@code https://tenant.sharepoint.com/sites/Test}.
     */
    @JsonProperty("siteUrl")
    private String siteUrl;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("webId")
    private String webId;

}
