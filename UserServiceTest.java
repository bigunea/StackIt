package service;

import dao.InMemoryUserDao;
import model.User;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit tests for {@link UserServiceImplementation}.
 *
 * Uses InMemoryUserDao as a fake DAO. Tests cover:
 *   - Validation rules (username format, email format, password length)
 *   - Uniqueness rules (no duplicate username, no duplicate email)
 *   - Login behavior (correct credentials, wrong password, missing user)
 *   - Password hashing (passwords are NEVER stored as plain text)
 */
public class UserServiceTest {

    private UserService service;

    @Before
    public void setUp() {
        service = new UserServiceImplementation(new InMemoryUserDao());
    }

    // Happy path 

    @Test
    public void register_withValidData_returnsUserWithId() {
        User u = service.register("alice", "alice@example.com", "secret123");
        assertNotNull(u);
        assertTrue("User should be assigned an ID", u.getId() > 0);
        assertEquals("alice", u.getUsername());
    }

    @Test
    public void register_storesPasswordAsHashNotPlainText() {
        User u = service.register("bob", "bob@example.com", "myPassword");

        assertNotEquals("Password must not be stored in plain text",
                "myPassword", u.getPasswordHash());
        assertTrue("Hash should be non-empty", u.getPasswordHash().length() > 10);
    }

    // Validation rules 

    @Test(expected = IllegalArgumentException.class)
    public void register_withTooShortUsername_throws() {
        service.register("ab", "test@example.com", "secret123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void register_withInvalidUsernameCharacters_throws() {
        service.register("bad name!", "x@y.com", "secret123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void register_withInvalidEmail_throws() {
        service.register("validUser", "not-an-email", "secret123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void register_withTooShortPassword_throws() {
        service.register("validUser", "test@example.com", "abc");
    }

    // Uniqueness rules 
    @Test
    public void register_withDuplicateUsername_throws() {
        service.register("charlie", "c1@example.com", "secret123");
        try {
            service.register("charlie", "c2@example.com", "secret123");
            fail("Should reject duplicate username");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("taken"));
        }
    }

    @Test
    public void register_withDuplicateEmail_throws() {
        service.register("dave", "shared@example.com", "secret123");
        try {
            service.register("eve", "shared@example.com", "secret123");
            fail("Should reject duplicate email");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("email"));
        }
    }

    // Login behavior 

    @Test
    public void login_withCorrectCredentials_returnsUser() {
        service.register("frank", "frank@example.com", "myPass99");
        User u = service.login("frank", "myPass99");

        assertNotNull("Login should succeed with correct credentials", u);
        assertEquals("frank", u.getUsername());
    }

    @Test
    public void login_withWrongPassword_returnsNull() {
        service.register("grace", "grace@example.com", "rightPass1");
        assertNull(service.login("grace", "wrongPass"));
    }

    @Test
    public void login_withUnknownUser_returnsNull() {
        assertNull(service.login("ghost", "anything"));
    }

    @Test
    public void login_withBlankUsername_returnsNull() {
        assertNull(service.login("", "anything"));
        assertNull(service.login(null, "anything"));
    }
}
