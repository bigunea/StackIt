package dao;

import model.User;
import java.util.HashMap;
import java.util.Map;

public class InMemoryUserDao implements UserDao {

    private final Map<String, User> byUsername = new HashMap<>();
    private final Map<String, User> byEmail    = new HashMap<>();
    private int nextId = 1;

    @Override
    public void add(User user) {
        user.setId(nextId++);
        byUsername.put(user.getUsername(), user);
        byEmail.put(user.getEmail(), user);
    }

    @Override
    public User findByUsername(String username) {
        return byUsername.get(username);
    }

    @Override
    public User findByEmail(String email) {
        return byEmail.get(email);
    }

    @Override
    public boolean usernameExists(String username) {
        return byUsername.containsKey(username);
    }

    @Override
    public boolean emailExists(String email) {
        return byEmail.containsKey(email);
    }
}