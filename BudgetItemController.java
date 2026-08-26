package controller;

import model.BudgetItem;
import service.BudgetItemService;
import util.Session;

import java.util.List;

/** Controller for BudgetItem CRUD. UI calls this only. */
public class BudgetItemController {

    private final BudgetItemService service;

    public BudgetItemController(BudgetItemService service) {
        this.service = service;
    }

    public void addItem(BudgetItem item, double budgetIncome) {
        item.setUserId(Session.getCurrentUser().getId());
        service.addItem(item, budgetIncome);
    }

    public List<BudgetItem> getItemsForBudget(int budgetId) {
        return service.getItemsForBudget(budgetId);
    }

    /** Filter option 2: filter items by category within a budget. */
    public List<BudgetItem> filterByCategory(int budgetId, String category) {
        return service.filterByCategory(budgetId, category);
    }

    public BudgetItem getItemById(int id) {
        return service.getItemById(id);
    }

    public void updateItem(BudgetItem item, double budgetIncome) {
        service.updateItem(item, budgetIncome);
    }

    public void deleteItem(int id) {
        service.deleteItem(id);
    }

    public double getTotalAllocated(int budgetId) {
        return service.getTotalAllocated(budgetId);
    }
}
