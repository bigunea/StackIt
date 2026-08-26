package service;

import model.Budget;
import java.util.List;

/** Service interface for Budget business logic. */
public interface BudgetService {

    void createBudget(Budget budget);

    List<Budget> getBudgetsForUser(int userId);

    /** Supports two filter options: name keyword and status. */
    List<Budget> searchBudgets(int userId, String keyword, String statusFilter);

    Budget getBudgetById(int id);

    void updateBudget(Budget budget);

    void deleteBudget(int id);
}
