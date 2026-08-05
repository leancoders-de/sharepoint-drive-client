package de.leancoders.sharepoint.service;

import com.google.common.collect.Maps;
import de.leancoders.sharepoint.request.SharepointDriveItemRole;
import de.leancoders.sharepoint.response.SharepointIds;
import de.leancoders.sharepoint.response.SharepointRoleDefinition;
import de.leancoders.sharepoint.response.SharepointRoleDefinitionsResponse;
import de.leancoders.sharepoint.response.SharepointSiteGroup;
import de.leancoders.sharepoint.response.SharepointSiteGroupsResponse;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * Grants permissions on drive items through the classic SharePoint REST API ({@code _api/web/...}).
 *
 * <p>Microsoft Graph cannot do this on a regular document library: {@code POST /permissions} only exists for
 * SharePoint Embedded containers, and {@code /invite} resolves recipients by email, so it can neither address a
 * SharePoint site group nor grant Full Control. Role assignments are SharePoint-native, so they need the
 * SharePoint API.
 *
 * <p>Authenticates through {@link SharepointCertificateClientService}, not through the Graph
 * {@link SharepointClientService}: tokens are audience bound, and SharePoint additionally refuses any app-only
 * token obtained with a client secret. The audience is derived from the site url, so no extra configuration is
 * needed beyond the certificate.
 *
 * <p>Typical use after creating a folder:
 * <pre>
 * final SharepointDriveItemResponse folder = driveService.createFolder(driveId, path, "Projekt X");
 * final SharepointIds ids = driveService.driveItemById(driveId, folder.getId()).getSharepointIds();
 *
 * restService.grantPermissions(ids, ImmutableMap.of(
 *     "117", SharepointDriveItemRole.WRITE,
 *     "119", SharepointDriveItemRole.OWNER
 * ), false);
 * </pre>
 */
public class SharepointRestClientService implements SharepointPaths {

    private static final String ODATA_NO_METADATA = "application/json;odata=nometadata";

    /**
     * Role definitions are web scoped and their ids are stable per site, so they are looked up once and cached.
     * Keyed by site url, then by {@code RoleTypeKind}.
     */
    @Nonnull
    private final Map<String, Map<Integer, Integer>> roleDefinitionCache = Maps.newConcurrentMap();

    @NonNull
    private final SharepointCertificateClientService certificateClientService;

    public SharepointRestClientService(@NonNull final SharepointCertificateClientService certificateClientService) {
        this.certificateClientService = certificateClientService;
    }

    // ---------------------------------------------------------------------------------------------------------
    // high level
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Applies an initial set of permissions to a drive item, replacing or extending what it inherits.
     *
     * <p>Breaks role inheritance first, then adds one role assignment per entry.
     *
     * @param ids                         the item's {@code sharepointIds} facet - fetch it via
     *                                    {@link SharepointDriveClientService#driveItemById(String, String)}
     * @param rolesByPrincipalId          site group principal id (e.g. {@code "117"}) to role. Principal ids are
     *                                    scoped to a single web, see {@link SharepointSiteGroup}
     * @param copyExistingRoleAssignments {@code false} starts from a clean slate - the item keeps <b>no</b>
     *                                    inherited assignments, so anyone not listed here (including the site's
     *                                    Owners and Members groups) loses access. Site collection administrators
     *                                    retain access regardless. {@code true} keeps what was inherited and
     *                                    layers these grants on top
     */
    public void grantPermissions(@NonNull final SharepointIds ids,
                                 @NonNull final Map<String, SharepointDriveItemRole> rolesByPrincipalId,
                                 final boolean copyExistingRoleAssignments) {

        checkArgument(!rolesByPrincipalId.isEmpty(), "at least one principal must be provided");
        requireCompleteIds(ids);

        post(ids, format(ids, SP_REST_BREAK_ROLE_INHERITANCE, copyExistingRoleAssignments, copyExistingRoleAssignments));


        for (final Map.Entry<String, SharepointDriveItemRole> roleAndPrincipalId : rolesByPrincipalId.entrySet()) {
            final String principalId = roleAndPrincipalId.getKey();
            final int roleDefinitionId = roleDefinitionId(ids.getSiteUrl(), roleAndPrincipalId.getValue());
            post(ids, format(ids, SP_REST_ADD_ROLE_ASSIGNMENT, principalId, roleDefinitionId));
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // role definitions
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Resolves the site specific {@code roledefid} for a role, matching on {@code RoleTypeKind} because permission
     * level names are localized (a German site reports {@code Vollzugriff}, not {@code Full Control}).
     *
     * @throws IllegalStateException if the site has no permission level of that type
     */
    public int roleDefinitionId(@NonNull final String siteUrl,
                                @NonNull final SharepointDriveItemRole role) {

        final Map<Integer, Integer> byRoleType =
            roleDefinitionCache.computeIfAbsent(normalizeSiteUrl(siteUrl), this::loadRoleDefinitions);

        final Integer roleDefinitionId = byRoleType.get(role.getRoleTypeKind());

        if (roleDefinitionId == null) {
            throw new IllegalStateException(
                "no permission level with RoleTypeKind %d (%s) on %s".formatted(role.getRoleTypeKind(), role, siteUrl));
        }

        return roleDefinitionId;
    }

    @Nonnull
    public SharepointRoleDefinitionsResponse roleDefinitions(@NonNull final String siteUrl) {
        return request(siteUrl)
            .log().all()
            .expect().statusCode(200)
            .log().all()
            .when()
            .get(normalizeSiteUrl(siteUrl) + SP_REST_ROLE_DEFINITIONS)
            .as(SharepointRoleDefinitionsResponse.class);
    }

    @Nonnull
    private Map<Integer, Integer> loadRoleDefinitions(@NonNull final String siteUrl) {
        final Map<Integer, Integer> byRoleType = Maps.newHashMap();

        for (final SharepointRoleDefinition definition : roleDefinitions(siteUrl).getValue()) {
            // custom permission levels report RoleTypeKind 0 and are not addressable by role
            if (definition.getRoleTypeKind() > 0) {
                byRoleType.putIfAbsent(definition.getRoleTypeKind(), definition.getId());
            }
        }

        return byRoleType;
    }

    // ---------------------------------------------------------------------------------------------------------
    // site groups
    // ---------------------------------------------------------------------------------------------------------

    /**
     * Lists the site's SharePoint groups. Use this to resolve a group's {@code principalId} when your app writes to
     * more than one site - the same group has a different principal id in every web.
     */
    @Nonnull
    public SharepointSiteGroupsResponse siteGroups(@NonNull final String siteUrl) {
        return request(siteUrl)
            .log().all()
            .expect().statusCode(200)
            .log().all()
            .when()
            .get(normalizeSiteUrl(siteUrl) + SP_REST_SITE_GROUPS)
            .as(SharepointSiteGroupsResponse.class);
    }

    @Nonnull
    public Optional<SharepointSiteGroup> siteGroupByTitle(@NonNull final String siteUrl,
                                                          @NonNull final String title) {
        return siteGroups(siteUrl)
            .getValue()
            .stream()
            .filter(group -> title.equals(group.getTitle()))
            .findFirst();
    }

    // ---------------------------------------------------------------------------------------------------------
    // role assignments
    // ---------------------------------------------------------------------------------------------------------

    public boolean hasUniqueRoleAssignments(@NonNull final SharepointIds ids) {
        requireCompleteIds(ids);

        return request(ids.getSiteUrl())
            .log().all()
            .expect().statusCode(200)
            .log().all()
            .when()
            .get(format(ids, SP_REST_HAS_UNIQUE_ROLE_ASSIGNMENTS))
            .jsonPath()
            .getBoolean("HasUniqueRoleAssignments");
    }

    /**
     * Puts the item back under its parent's permissions, discarding its unique assignments.
     */
    public void resetRoleInheritance(@NonNull final SharepointIds ids) {
        requireCompleteIds(ids);

        post(ids, format(ids, SP_REST_RESET_ROLE_INHERITANCE));
    }


    /**
     * Revokes a permission level from a principal. To change a level, remove the old one and add the new one.
     */
    public void removeRoleAssignment(@NonNull final SharepointIds ids,
                                     @NonNull final String principalId,
                                     final int roleDefinitionId) {
        requireCompleteIds(ids);

        post(ids, format(ids, SP_REST_REMOVE_ROLE_ASSIGNMENT, principalId, roleDefinitionId));
    }

    public void removeRoleAssignment(@NonNull final SharepointIds ids,
                                     @NonNull final String principalId,
                                     @NonNull final SharepointDriveItemRole role) {
        removeRoleAssignment(ids, principalId, roleDefinitionId(ids.getSiteUrl(), role));
    }

    // ---------------------------------------------------------------------------------------------------------
    // plumbing
    // ---------------------------------------------------------------------------------------------------------

    private void post(@NonNull final SharepointIds ids, @NonNull final String url) {
        request(ids.getSiteUrl())
            .contentType(ContentType.JSON)
            // SharePoint expects an explicit Content-Length: 0 on these action posts
            .body("")
            .log().all()
            .expect().statusCode(anyOf(is(200), is(201), is(204)))
            .log().all()
            .when()
            .post(url)
        ;
    }

    /**
     * A request authorized for the SharePoint audience rather than Graph. Url encoding stays off because the paths
     * carry literal parentheses and single quotes.
     *
     * <p>Deliberately not built via {@link SharepointAuthContext#authorizedRequest()}: that pins
     * {@code Accept: application/json}, and RestAssured appends headers rather than replacing them, which would
     * leave the request with two Accept values and let SharePoint pick the odata format.
     */
    @Nonnull
    private RequestSpecification request(@NonNull final String siteUrl) {
        final SharepointAuthContext context = certificateClientService.validateAndGetForResource(resourceRootOf(siteUrl));

        return context.getRequestSpecification()
            .get()
            .header("Authorization", "Bearer " + context.getToken().getAccessToken())
            .accept(ODATA_NO_METADATA)
            .urlEncodingEnabled(false);
    }

    @Nonnull
    private String format(@NonNull final SharepointIds ids,
                          @NonNull final String pattern,
                          final Object... arguments) {

        final Object[] allArguments = new Object[arguments.length + 2];
        allArguments[0] = ids.getListId();
        allArguments[1] = ids.getListItemId();
        System.arraycopy(arguments, 0, allArguments, 2, arguments.length);

        return normalizeSiteUrl(ids.getSiteUrl()) + String.format(pattern, allArguments);
    }

    private void requireCompleteIds(@NonNull final SharepointIds ids) {
        checkArgument(!isNullOrEmpty(ids.getSiteUrl()), "sharepointIds.siteUrl is missing");
        checkArgument(!isNullOrEmpty(ids.getListId()), "sharepointIds.listId is missing");
        checkArgument(!isNullOrEmpty(ids.getListItemId()), "sharepointIds.listItemId is missing");
    }

    /**
     * Derives the token audience from a site url, e.g. {@code https://tenant.sharepoint.com/sites/Test} yields
     * {@code https://tenant.sharepoint.com}.
     */
    @Nonnull
    static String resourceRootOf(@NonNull final String siteUrl) {
        final URI uri = URI.create(siteUrl);

        checkArgument(uri.getScheme() != null && uri.getHost() != null, "not an absolute site url: %s", siteUrl);

        return uri.getScheme() + "://" + uri.getHost();
    }

    @Nonnull
    private static String normalizeSiteUrl(@NonNull final String siteUrl) {
        return siteUrl.endsWith("/")
            ? siteUrl.substring(0, siteUrl.length() - 1)
            : siteUrl;
    }
}
