package de.leancoders.sharepoint.service;

public interface SharepointPaths {

    String OAUTH_AUTH_BASE = "/{tenantId}/oauth2/v2.0/";
    String OAUTH_AUTH__TOKEN = OAUTH_AUTH_BASE + "token/";

    // -----------------------------------------------------------------------------------------------------------
    // classic SharePoint REST API - appended to a site url, e.g. https://tenant.sharepoint.com/sites/Test
    //
    // These are String.format patterns rather than RestAssured path templates: the paths contain literal
    // parentheses and single quotes that must not be percent encoded.
    // -----------------------------------------------------------------------------------------------------------

    String SP_REST_WEB = "/_api/web";

    String SP_REST_ROLE_DEFINITIONS = SP_REST_WEB + "/roledefinitions";
    String SP_REST_SITE_GROUPS = SP_REST_WEB + "/sitegroups";

    /** args: listId, listItemId */
    String SP_REST_LIST_ITEM = SP_REST_WEB + "/lists(guid'%s')/items(%s)";

    /** args: listId, listItemId */
    String SP_REST_HAS_UNIQUE_ROLE_ASSIGNMENTS = SP_REST_LIST_ITEM + "?$select=HasUniqueRoleAssignments";

    /** args: listId, listItemId, copyRoleAssignments, clearSubscopes */
    String SP_REST_BREAK_ROLE_INHERITANCE =
        SP_REST_LIST_ITEM + "/breakroleinheritance(copyRoleAssignments=%b,clearSubscopes=%b)";

    /** args: listId, listItemId */
    String SP_REST_RESET_ROLE_INHERITANCE = SP_REST_LIST_ITEM + "/resetroleinheritance";

    /** args: listId, listItemId, principalId, roleDefinitionId */
    String SP_REST_ADD_ROLE_ASSIGNMENT =
        SP_REST_LIST_ITEM + "/roleassignments/addroleassignment(principalid=%s,roledefid=%d)";

    /** args: listId, listItemId, principalId, roleDefinitionId */
    String SP_REST_REMOVE_ROLE_ASSIGNMENT =
        SP_REST_LIST_ITEM + "/roleassignments/removeroleassignment(principalid=%s,roledefid=%d)";

}
