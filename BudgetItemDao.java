package dao;

import model.BudgetItem;
import java.util.List;

/** DAO interface for BudgetItem persistence. */
public interface BudgetItemDao{

    void add(BudgetItem item);

    /** All items for a given budget, ordered by item_date DESC. */
    List<BudgetItem> getByBudgetId(int budgetId);

    BudgetItem getById(int id);

    void update(BudgetItem item);

    void delete(int id);

    /** Total amount allocated to a budget (SUM of all its items). */
    double getTotalAllocated(int budgetId);

    /** Filter items by category within a budget. */
    List<BudgetItem> filterByCategory(int budgetId, String category);
}
