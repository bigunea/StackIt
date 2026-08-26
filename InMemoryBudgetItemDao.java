package dao;

import model.BudgetItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In-memory implementation of BudgetItemDAO.
 * Stores budget items in an ArrayList.
 * Used for the demo version of StackIt (pre-SQL phase).
 */
public class InMemoryBudgetItemDao implements BudgetItemDao {

    private final List<BudgetItem> items = new ArrayList<>();
    private int nextId = 1;

    @Override
    public void add(BudgetItem item) {
        item.setId(nextId++);
        items.add(item);
    }

    @Override
    public List<BudgetItem> getByBudgetId(int budgetId) {
        // Return all items belonging to the given budget, newest first
        return items.stream()
                .filter(i -> i.getBudgetId() == budgetId)
                .sorted((a, b) -> b.getItemDate().compareTo(a.getItemDate()))
                .collect(Collectors.toList());
    }

    @Override
    public BudgetItem getById(int id) {
        return items.stream()
                .filter(i -> i.getId() == id)
                .findFirst().orElse(null);
    }

    @Override
    public void update(BudgetItem item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == item.getId()) {
                items.set(i, item);
                return;
            }
        }
    }

    @Override
    public void delete(int id) {
        items.removeIf(i -> i.getId() == id);
    }

    @Override
    public double getTotalAllocated(int budgetId) {
        return items.stream()
                .filter(i -> i.getBudgetId() == budgetId)
                .mapToDouble(BudgetItem::getAmount)
                .sum();
    }

    @Override
    public List<BudgetItem> filterByCategory(int budgetId, String category) {
        return items.stream()
                .filter(i -> i.getBudgetId() == budgetId
                          && i.getCategory().name().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
}
