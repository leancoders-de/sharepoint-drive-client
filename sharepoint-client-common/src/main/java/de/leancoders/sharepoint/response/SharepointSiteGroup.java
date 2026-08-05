package de.leancoders.sharepoint.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * A classic SharePoint <i>site</i> group as returned by {@code GET {siteUrl}/_api/web/sitegroups}.
 *
 * <p>Not to be confused with {@link SharepointGroup}, which is an Entra ID (Azure AD) group from
 * {@code GET /v1.0/groups}. Site groups live in the site's user information list, are invisible to Microsoft
 * Graph's write APIs, and their {@link #id} is the {@code principalid} used by {@code addroleassignment}.
 *
 * <p><b>{@link #id} is scoped to a single web.</b> The same group in another site collection has a different
 * number, which is why Graph encodes it as {@code base64(siteGuid + "_" + principalId)}.
 */
@Data
public class SharepointSiteGroup {

    /**
     * The site-scoped principal id - the {@code principalid} argument for {@code addroleassignment}.
     */
    @JsonProperty("Id")
    private int id;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("LoginName")
    private String loginName;

    @JsonProperty("Description")
    private String description;

    /**
     * {@code 8} for a SharePoint group, {@code 1} for a user, {@code 4} for a security group.
     */
    @JsonProperty("PrincipalType")
    private int principalType;

    @JsonProperty("IsHiddenInUI")
    private boolean hiddenInUi;

    @JsonProperty("OwnerTitle")
    private String ownerTitle;

}
