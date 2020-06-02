package com.inspur.health;

import sdk.security.service.IAuthenticationProvider;

import java.util.Map;

public class AuthenticationProviderImpl implements IAuthenticationProvider {

    @Override
    public String getLoginUserId() {
        return "admin";
    }

    @Override
    public String getToken() {
        return null;
    }

    @Override
    public String getKrbPrincipalName() {
        return null;
    }

    @Override
    public Map<String, String> getLoginUserInfo() {
        return null;
    }
}