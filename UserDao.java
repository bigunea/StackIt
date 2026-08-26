package dao;

import model.User;

/** DAO interface for User persistence. */
public interface UserDao {
    void add(User user);
    User findByUsername(String username);
    User findByEmail(String email);
    boolean usernameExists(String username);
    boolean emailExists(String email);
}
