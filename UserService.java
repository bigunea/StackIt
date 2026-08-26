package service;

import model.User;

/** Service interface for user registration and authentication. */
public interface UserService {

    /**
     * Register a new user. Throws IllegalArgumentException on validation failure.
     * Passwords are hashed before storage – never stored as plain text.
     */
    User register(String username, String email, String plainPassword);

    /**
     * Authenticate a user. Returns the User on success, null on failure.
     * Business rule: after 5 failed attempts the account is temporarily locked.
     */
    User login(String username, String plainPassword);
}
