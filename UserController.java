package controller;

import model.User;
import service.UserService;

/** Controller for user registration and authentication. UI calls this only. */
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Returns the registered User or throws IllegalArgumentException with a friendly message. */
    public User register(String username, String email, String password) {
        return userService.register(username, email, password);
    }

    /** Returns the User on successful login, null if credentials are wrong. */
    public User login(String username, String password) {
        return userService.login(username, password);
    }
}
