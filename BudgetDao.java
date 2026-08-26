package dao;

import model.Budget;
import java.util.List;

/** DAO interface for Budget persistence. All SQL/JDBC logic lives in implementations. */
public interface BudgetDao{

    void add(Budget budget);

    /** Returns all budgets owned by the given user. */
    List<Budget> getByUserId(int userId);

    Budget getById(int id);

    void update(Budget budget);

    void delete(int id);

    /**
     * Non-trivial query: JOIN budgets with budget_items, filter by name keyword
     * and optional status, recalculating total_allocated via SUM(bi.amount).
     * Supports both search fields simultaneously.
     *
     * @param userId      owner
     * @param keyword     partial name match (empty = match all)
     * @param statusFilter "ALL", "ACTIVE", or "ARCHIVED"
     */
    List<Budget> search(int userId, String keyword, String statusFilter);

    /** Returns true if another budget with the same name exists for this user (excluding given id). */
    boolean existsByNameForUser(String name, int userId, int excludeId);
}
