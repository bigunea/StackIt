package service;

import dao.BudgetDao;
import model.Budget;
import java.util.List;

/**
 * Budget service with these business rules:
 *  1. Name, date-range, and income validation (prevent bad data entering DB).
 *  2. Budget name must be unique per user (prevents duplicates in same account).
 */
public class BudgetServiceImplementation implements BudgetService {

    private final BudgetDao dao;

    public BudgetServiceImplementation(BudgetDao dao) {
        this.dao = dao;
    }

    @Override
    public void createBudget(Budget budget) {
        validateBudget(budget);

        // Business rule 2: unique name per user
        if (dao.existsByNameForUser(budget.getName(), budget.getUserId(), 0))
            throw new IllegalArgumentException(
                "You already have a budget named \"" + budget.getName() + "\".");

        dao.add(budget);
    }

    @Override
    public List<Budget> getBudgetsForUser(int userId) {
        return dao.getByUserId(userId);
    }

    @Override
    public List<Budget> searchBudgets(int userId, String keyword, String statusFilter) {
        return dao.search(userId, keyword, statusFilter);
    }

    @Override
    public Budget getBudgetById(int id) {
        return dao.getById(id);
    }

    @Override
    public void updateBudget(Budget budget) {
        validateBudget(budget);

        // Business rule 2: unique name per user (exclude self)
        if (dao.existsByNameForUser(budget.getName(), budget.getUserId(), budget.getId()))
            throw new IllegalArgumentException(
                "You already have a budget named \"" + budget.getName() + "\".");

        dao.update(budget);
    }

    @Override
    public void deleteBudget(int id) {
        dao.delete(id);
    }

    // Shared validation (business rule 1) 
    private void validateBudget(Budget budget) {
        if (budget.getName() == null || budget.getName().isBlank())
            throw new IllegalArgumentException("Budget name is required.");

        if (!budget.getName().matches("[a-zA-Z0-9 \\-_']+"))
            throw new IllegalArgumentException(
                "Budget name may only contain letters, numbers, spaces, hyphens, or apostrophes.");

        if (budget.getStartDate() == null || budget.getEndDate() == null)
            throw new IllegalArgumentException("Start and end dates are required.");

        if (!budget.getEndDate().isAfter(budget.getStartDate()))
            throw new IllegalArgumentException("End date must be after start date.");

        if (budget.getTotalIncome() <= 0)
            throw new IllegalArgumentException("Total income must be greater than zero.");
    }
}
