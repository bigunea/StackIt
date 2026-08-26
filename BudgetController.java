package controller;

import model.Budget;
import service.BudgetService;
import util.Session;

import java.util.List;

/**
 * Controller for Budget operations.
 * Reads the current user from Session – UI never touches Session directly.
 */
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    public void createBudget(Budget budget) {
        budget.setUserId(Session.getCurrentUser().getId());
        service.createBudget(budget);
    }

    public List<Budget> getBudgetsForCurrentUser() {
        return service.getBudgetsForUser(Session.getCurrentUser().getId());
    }

    /**
     * Supports two search/filter options simultaneously:
     *   keyword: partial name match
     *   statusFilter: "ALL", "ACTIVE", or "ARCHIVED"
     */
    public List<Budget> searchBudgets(String keyword, String statusFilter) {
        return service.searchBudgets(Session.getCurrentUser().getId(), keyword, statusFilter);
    }

    public Budget getBudgetById(int id) {
        return service.getBudgetById(id);
    }

    public void updateBudget(Budget budget) {
        service.updateBudget(budget);
    }

    public void deleteBudget(int id) {
        service.deleteBudget(id);
    }
}
