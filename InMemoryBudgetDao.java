package dao;

import model.Budget;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory DAO for Budget entities.
 *
 * Keeps the {@code totalAllocated} field in sync with the actual sum of items
 * stored in {@link InMemoryBudgetItemDAO}. This mirrors what the JDBC version
 * does with {@code SUM(amount) GROUP BY budget_id}.
 */
public class InMemoryBudgetDao implements BudgetDao {

    private final List<Budget> budgets = new ArrayList<>();
    private int nextId = 1;

    /** Optional reference to the item DAO so we can compute totalAllocated. */
    private final BudgetItemDao itemDao;

    /** Default constructor (used by tests with fake DAOs). */
    public InMemoryBudgetDao() {
        this.itemDao = null;
    }

    /** Production constructor — needed so getById/search show real allocated totals. */
    public InMemoryBudgetDao(BudgetItemDao itemDao) {
        this.itemDao = itemDao;
    }

    @Override
    public void add(Budget budget) {
        budget.setId(nextId++);
        budgets.add(budget);
    }

    @Override
    public List<Budget> getByUserId(int userId) {
        return budgets.stream()
                .filter(b -> b.getUserId() == userId)
                .map(this::syncAllocated)
                .collect(Collectors.toList());
    }

    @Override
    public Budget getById(int id) {
        return budgets.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .map(this::syncAllocated)
                .orElse(null);
    }

    @Override
    public void update(Budget budget) {
        for (int i = 0; i < budgets.size(); i++) {
            if (budgets.get(i).getId() == budget.getId()) {
                budgets.set(i, budget);
                return;
            }
        }
    }

    @Override
    public void delete(int id) {
        budgets.removeIf(b -> b.getId() == id);
    }

    @Override
    public List<Budget> search(int userId, String keyword, String statusFilter) {
        String kw = keyword == null ? "" : keyword.toLowerCase();
        return budgets.stream()
                .filter(b -> b.getUserId() == userId)
                .filter(b -> b.getName().toLowerCase().contains(kw))
                .filter(b -> statusFilter == null || statusFilter.equalsIgnoreCase("ALL")
                             || b.getStatus().name().equalsIgnoreCase(statusFilter))
                .map(this::syncAllocated)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNameForUser(String name, int userId, int excludeId) {
        return budgets.stream()
                .anyMatch(b -> b.getUserId() == userId
                               && b.getName().equalsIgnoreCase(name)
                               && b.getId() != excludeId);
    }

    /**
     * Recomputes {@code totalAllocated} on the budget by summing all of its
     * items in the item DAO. Equivalent to:
     *   SELECT SUM(amount) FROM budget_items WHERE budget_id = ?
     */
    private Budget syncAllocated(Budget b) {
        if (itemDao != null) {
            b.setTotalAllocated(itemDao.getTotalAllocated(b.getId()));
        }
        return b;
    }
}
