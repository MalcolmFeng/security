package com.inspur.health;

import java.util.List;
import java.util.Map;
import sdk.security.service.IAuthorizationProvider;

public class AuthorizationProviderImpl implements IAuthorizationProvider {

    @Override
    public boolean hasPermission(String resourceId) {
        return true;
    }

    @Override
    public List<Map<String, String>> getResources(String type) {
        return null;
    }

}
