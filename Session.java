package util;

import model.User;

/**
 * Application-scoped session. Holds the currently authenticated user.
 * Set after a successful login; cleared on logout.
 */
public class Session {

    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
