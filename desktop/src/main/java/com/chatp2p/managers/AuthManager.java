package com.chatp2p.managers;

import com.chatp2p.core.App;
import com.chatp2p.models.UserProfile;
import com.chatp2p.exceptions.*;

public class AuthManager {

    public static void logoutSynchronous() {
        UserProfile profile = App.getUserProfile();
        if (profile == null || profile.getAuthToken() == null) return;
        try {
            HttpManager.postWithToken(
                    "http://localhost:8080/api/auth/logout",
                    profile.getAuthToken(),
                    ""
            );
        } catch (Exception e) {
            throw new NetworkException("Failed to logout user", e);
        }
    }
}