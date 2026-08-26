package service;

import java.util.List;

import model.BudgetItem;

/**
 * BudgetItem service with business rules:
 *  1. Amount must be > 0 and description must be provided.
 *  2. Adding or updating an item must not push total allocated above the budget income
 *     (prevents overspending – the "over-budget guard").
 */
public class BudgetItemServiceImplementation implements BudgetItemService {

    private final dao.BudgetItemDao dao;

    public BudgetItemServiceImplementation(dao.BudgetItemDao dao) {
        this.dao = dao;
    }

    @Override
    public void addItem(BudgetItem item, double budgetIncome) {
        validateItem(item);

        // Business rule 2: over-budget guard
        double currentTotal = dao.getTotalAllocated(item.getBudgetId());
        if (currentTotal + item.getAmount() > budgetIncome)
            throw new IllegalArgumentException(String.format(
                "This item ($%.2f) would exceed the budget income. " +
                "Remaining: $%.2f", item.getAmount(), budgetIncome - currentTotal));

        dao.add(item);
    }

    @Override
    public List<BudgetItem> getItemsForBudget(int budgetId) {
        return dao.getByBudgetId(budgetId);
    }

    @Override
    public List<BudgetItem> filterByCategory(int budgetId, String category) {
        return dao.filterByCategory(budgetId, category);
    }

    @Override
    public BudgetItem getItemById(int id) {
        return dao.getById(id);
    }

    @Override
    public void updateItem(BudgetItem item, double budgetIncome) {
        validateItem(item);

        // Over-budget guard: total excluding old value of this item + new amount
        BudgetItem old = dao.getById(item.getId());
        double oldAmount = (old != null) ? old.getAmount() : 0;
        double currentTotal = dao.getTotalAllocated(item.getBudgetId());
        double projectedTotal = currentTotal - oldAmount + item.getAmount();
        if (projectedTotal > budgetIncome)
            throw new IllegalArgumentException(String.format(
                "This update would exceed the budget income. " +
                "Available: $%.2f", budgetIncome - (currentTotal - oldAmount)));

        dao.update(item);
    }

    @Override
    public void deleteItem(int id) {
        dao.delete(id);
    }

    @Override
    public double getTotalAllocated(int budgetId) {
        return dao.getTotalAllocated(budgetId);
    }

    // Validation 
    private void validateItem(BudgetItem item) {
        if (item.getAmount() <= 0)
            throw new IllegalArgumentException("Item amount must be greater than zero.");
        if (item.getDescription() == null || item.getDescription().isBlank())
            throw new IllegalArgumentException("Item description is required.");
        if (item.getItemDate() == null)
            throw new IllegalArgumentException("Item date is required.");
    }
}
