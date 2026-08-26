package service;

import dao.UserDao;
import model.User;
import util.PasswordUtils;

/**
 * Implements registration and login with business rules:
 *  1. Username must be 3–20 alphanumeric characters.
 *  2. Email must match a basic pattern.
 *  3. Password must be at least 6 characters.
 *  4. Username and email must be unique.
 *  5. Passwords are SHA-256 + salted before storage – never plain text.
 */
public class UserServiceImplementation implements UserService {

    private final UserDao userDAO;

    public UserServiceImplementation(UserDao userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User register(String username, String email, String plainPassword) {

        // Validation
        if (username == null || username.isBlank())
            throw new IllegalArgumentException("Username is required.");
        if (!username.matches("[a-zA-Z0-9_]{3,20}"))
            throw new IllegalArgumentException(
                "Username must be 3–20 characters (letters, numbers, underscores).");

        if (email == null || !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Please enter a valid email address.");

        if (plainPassword == null || plainPassword.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters.");

        //  Uniqueness checks (business rules)
        if (userDAO.usernameExists(username))
            throw new IllegalArgumentException("Username \"" + username + "\" is already taken.");

        if (userDAO.emailExists(email))
            throw new IllegalArgumentException("An account with that email already exists.");

        //  Hash & persist
        String hash = PasswordUtils.hashPassword(plainPassword);
        User user = new User(username, email, hash);
        userDAO.add(user);
        return user;
    }

    @Override
    public User login(String username, String plainPassword) {
        if (username == null || username.isBlank() || plainPassword == null)
            return null;

        User user = userDAO.findByUsername(username);
        if (user == null) return null;

        return PasswordUtils.verifyPassword(plainPassword, user.getPasswordHash())
               ? user : null;
    }
}
