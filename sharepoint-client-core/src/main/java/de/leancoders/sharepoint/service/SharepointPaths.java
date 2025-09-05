package de.leancoders.sharepoint.service;

public interface SharepointPaths {

    // https://login.microsoftonline.com/203183bd-2463-4ce3-88e5-1e311eb475f9/oauth2/v2.0/token

    String OAUTH_AUTH_BASE = "/{tenantId}/oauth2/v2.0/";
    String OAUTH_AUTH__TOKEN = OAUTH_AUTH_BASE + "token/";

}
