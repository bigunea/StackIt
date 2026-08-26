package dao;

import model.User;
import util.DBConnection;

import java.sql.*;

/**
 * JDBC implementation of {@link UserDao}.
 * All SQL/JDBC logic for users lives here.
 *
 * Uses PreparedStatement (parameterised queries) to prevent SQL injection
 * and try-with-resources so connections always close.
 */
public class JdbcUserDao implements UserDao {

    @Override
    public void add(User user) {
        String sql = "INSERT INTO users (username, email, password_hash) " +
                     "VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to add user: " + ex.getMessage(), ex);
        }
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT id, username, email, password_hash " +
                     "FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find user: " + ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT id, username, email, password_hash " +
                     "FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to find user: " + ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public boolean usernameExists(String username) {
        return countWhere("username", username) > 0;
    }

    @Override
    public boolean emailExists(String email) {
        return countWhere("email", email) > 0;
    }

    private int countWhere(String column, String value) {
        String sql = "SELECT COUNT(*) FROM users WHERE " + column + " = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Lookup failed: " + ex.getMessage(), ex);
        }
    }

    /** Build a User from the current ResultSet row. */
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User(
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password_hash")
        );
        u.setId(rs.getInt("id"));
        return u;
    }
}
