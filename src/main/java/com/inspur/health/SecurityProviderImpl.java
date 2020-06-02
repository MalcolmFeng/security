package com.inspur.health;

import sdk.security.service.ISecurityProvider;

public class SecurityProviderImpl implements ISecurityProvider {

    private static String contextUrl = getIdpContext();

    private static String getIdpContext() {
        return contextUrl;
    }

    @Override
    public String getSecurityContextUrl() {
        return contextUrl;
    }

    @Override
    public String getLogoutUrl(String backUrl) {
        return getSecurityContextUrl() + "/logout";
    }
}
