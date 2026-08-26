package service;

import model.BudgetItem;
import java.util.List;

/** Service interface for BudgetItem business logic. */
public interface BudgetItemService {

    void addItem(BudgetItem item, double budgetIncome);

    List<BudgetItem> getItemsForBudget(int budgetId);

    List<BudgetItem> filterByCategory(int budgetId, String category);

    BudgetItem getItemById(int id);

    void updateItem(BudgetItem item, double budgetIncome);

    void deleteItem(int id);

    double getTotalAllocated(int budgetId);
}
