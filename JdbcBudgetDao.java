package dao;

import model.Budget;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link BudgetDao}.
 *
 * Highlight: the {@link #search(int, String, String)} method uses a
 * LEFT JOIN with budget_items + GROUP BY to compute total_allocated
 * directly in SQL. This is the project's non-trivial query.
 */
public class JdbcBudgetDao implements BudgetDao {

    /** Reusable SELECT clause that joins items and computes allocated total. */
    private static final String BASE_SELECT =
        "SELECT b.id, b.user_id, b.name, b.start_date, b.end_date, " +
        "       b.total_income, b.status, " +
        "       COALESCE(SUM(bi.amount), 0) AS total_allocated " +
        "FROM budgets b " +
        "LEFT JOIN budget_items bi ON b.id = bi.budget_id ";

    private static final String GROUP_ORDER =
        "GROUP BY b.id, b.user_id, b.name, b.start_date, b.end_date, " +
        "         b.total_income, b.status " +
        "ORDER BY b.start_date DESC ";

    @Override
    public void add(Budget budget) {
        String sql = "INSERT INTO budgets (user_id, name, start_date, end_date, " +
                     "total_income, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1,    budget.getUserId());
            ps.setString(2, budget.getName());
            ps.setDate(3,   Date.valueOf(budget.getStartDate()));
            ps.setDate(4,   Date.valueOf(budget.getEndDate()));
            ps.setDouble(5, budget.getTotalIncome());
            ps.setString(6, budget.getStatus().name());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) budget.setId(keys.getInt(1));
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to add budget: " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Budget> getByUserId(int userId) {
        String sql = BASE_SELECT + "WHERE b.user_id = ? " + GROUP_ORDER;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Budget> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load budgets: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Budget getById(int id) {
        String sql = BASE_SELECT + "WHERE b.id = ? " + GROUP_ORDER;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load budget: " + ex.getMessage(), ex);
        }
        return null;
    }

    @Override
    public void update(Budget budget) {
        String sql = "UPDATE budgets SET name = ?, start_date = ?, end_date = ?, " +
                     "total_income = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, budget.getName());
            ps.setDate(2,   Date.valueOf(budget.getStartDate()));
            ps.setDate(3,   Date.valueOf(budget.getEndDate()));
            ps.setDouble(4, budget.getTotalIncome());
            ps.setString(5, budget.getStatus().name());
            ps.setInt(6,    budget.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update budget: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(int id) {
        // Items are removed automatically via ON DELETE CASCADE in schema.sql
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to delete budget: " + ex.getMessage(), ex);
        }
    }

    /**
     * Non-trivial query: combines a LEFT JOIN, SUM aggregation, GROUP BY,
     * a LIKE filter on name, and an optional status filter — all in one query.
     */
    @Override
    public List<Budget> search(int userId, String keyword, String statusFilter) {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        sql.append("WHERE b.user_id = ? ");
        sql.append("AND b.name LIKE ? ");
        boolean filterStatus = statusFilter != null
                              && !statusFilter.equalsIgnoreCase("ALL");
        if (filterStatus) sql.append("AND b.status = ? ");
        sql.append(GROUP_ORDER);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            String kw = (keyword == null) ? "" : keyword;
            ps.setInt(1, userId);
            ps.setString(2, "%" + kw + "%");
            if (filterStatus) ps.setString(3, statusFilter.toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                List<Budget> list = new ArrayList<>();
                while (rs.next()) list.add(mapRow(rs));
                return list;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Search failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean existsByNameForUser(String name, int userId, int excludeId) {
        String sql = "SELECT COUNT(*) FROM budgets " +
                     "WHERE user_id = ? AND LOWER(name) = LOWER(?) AND id <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1,    userId);
            ps.setString(2, name);
            ps.setInt(3,    excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            throw new RuntimeException("Name lookup failed: " + ex.getMessage(), ex);
        }
    }

    /** Build a Budget object from the current ResultSet row. */
    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget b = new Budget(
            rs.getInt("user_id"),
            rs.getString("name"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate(),
            rs.getDouble("total_income")
        );
        b.setId(rs.getInt("id"));
        b.setTotalAllocated(rs.getDouble("total_allocated"));
        b.setStatus(Budget.Status.valueOf(rs.getString("status")));
        return b;
    }
}
