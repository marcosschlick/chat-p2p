package com.chatp2p.managers;

public class AuthManager {
    private static String authToken;
    private static String currentUser;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static void setCurrentUser(String user) {
        currentUser = user;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void logoutSynchronous() {
        if (authToken == null) return;

        try {
            HttpManager.postWithToken(
                    "http://localhost:8080/api/auth/logout",
                    authToken,
                    ""
            );
            System.out.println("Logout realizado com sucesso");
        } catch (Exception e) {
            System.err.println("Erro no logout: " + e.getMessage());
        }
    }
}