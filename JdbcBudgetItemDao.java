package dao;

import model.BudgetItem;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BudgetItemDao}.
 * Uses PreparedStatement and try-with-resources throughout.
 */
public class JdbcBudgetItemDao implements BudgetItemDao {

    private static final String BASE_SELECT =
        "SELECT id, budget_id, user_id, category, amount, description, item_date " +
        "FROM budget_items ";

    @Override
    public void add(BudgetItem item) {
        String sql = "INSERT INTO budget_items " +
                     "(budget_id, user_id, category, amount, description, item_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    item.getBudgetId());
            ps.setInt(2,    item.getUserId());
            ps.setString(3, item.getCategory().name());
            ps.setDouble(4, item.getAmount());
            ps.setString(5, item.getDescription());
            ps.setDate(6,   Date.valueOf(item.getItemDate()));
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) item.setId(keys.getInt(1));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to add expense: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<BudgetItem> getByBudgetId(int budgetId) {
        String sql = BASE_SELECT + "WHERE budget_id = ? ORDER BY item_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, budgetId);
            try (ResultSet rs = ps.executeQuery()) {
                List<BudgetItem> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load expenses: " + ex.getMessage(), ex);
        }
    }

    @Override
    public BudgetItem getById(int id) {
        String sql = BASE_SELECT + "WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load expense: " + ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public void update(BudgetItem item) {
        String sql = "UPDATE budget_items SET category = ?, amount = ?, " +
                     "description = ?, item_date = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getCategory().name());
            ps.setDouble(2, item.getAmount());
            ps.setString(3, item.getDescription());
            ps.setDate(4,   Date.valueOf(item.getItemDate()));
            ps.setInt(5,    item.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update expense: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM budget_items WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete expense: " + ex.getMessage(), ex);
        }
    }

    @Override
    public double getTotalAllocated(int budgetId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM budget_items " +
                     "WHERE budget_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, budgetId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Sum failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<BudgetItem> filterByCategory(int budgetId, String category) {
        String sql = BASE_SELECT +
                     "WHERE budget_id = ? AND category = ? " +
                     "ORDER BY item_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    budgetId);
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                List<BudgetItem> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Filter failed: " + ex.getMessage(), ex);
        }
    }

    /** Build a BudgetItem from the current ResultSet row. */
    private BudgetItem mapRow(ResultSet rs) throws SQLException {
        BudgetItem item = new BudgetItem(
            rs.getInt("budget_id"),
            rs.getInt("user_id"),
            BudgetItem.Category.valueOf(rs.getString("category")),
            rs.getDouble("amount"),
            rs.getString("description"),
            rs.getDate("item_date").toLocalDate()
        );
        item.setId(rs.getInt("id"));
        return item;
    }
}
