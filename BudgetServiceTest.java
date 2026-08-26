package service;

import dao.InMemoryBudgetDao;
import model.Budget;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**
 * JUnit tests for {@link BudgetServiceImplementation}.
 *
 * Uses InMemoryBudgetDao as a fake DAO so tests run without a database.
 * Each test creates a fresh service instance to avoid shared state.
 *
 * Covers both business rules:
 *   1. Validation (name format, dates, income must be positive)
 *   2. Unique-name-per-user rule
 */
public class BudgetServiceTest {

    private BudgetService service;
    private static final int USER_ID = 1;

    @Before
    public void setUp() {
        service = new BudgetServiceImplementation(new InMemoryBudgetDao());
    }

    /** Helper: build a valid Budget for tests. */
    private Budget validBudget(String name) {
        return new Budget(USER_ID, name,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                3000.00);
    }

    // Happy path 

    @Test
    public void createBudget_withValidData_persistsAndAssignsId() {
        Budget b = validBudget("January Budget");
        service.createBudget(b);

        assertTrue("ID should be assigned after create", b.getId() > 0);
        assertEquals(1, service.getBudgetsForUser(USER_ID).size());
    }

    @Test
    public void getBudgetsForUser_returnsOnlyThatUsersBudgets() {
        service.createBudget(validBudget("January"));
        service.createBudget(validBudget("February"));

        List<Budget> mine = service.getBudgetsForUser(USER_ID);
        List<Budget> none = service.getBudgetsForUser(999);

        assertEquals(2, mine.size());
        assertTrue("Other user should see none", none.isEmpty());
    }

    // Business rule 1: validation

    @Test(expected = IllegalArgumentException.class)
    public void createBudget_withBlankName_throws() {
        service.createBudget(validBudget(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBudget_withInvalidNameCharacters_throws() {
        service.createBudget(validBudget("Bad@Name#1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBudget_withEndBeforeStart_throws() {
        Budget b = new Budget(USER_ID, "Bad Dates",
                LocalDate.of(2025, 5, 31),
                LocalDate.of(2025, 5, 1),
                3000.00);
        service.createBudget(b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBudget_withZeroIncome_throws() {
        Budget b = new Budget(USER_ID, "Zero Income",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                0.00);
        service.createBudget(b);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBudget_withNegativeIncome_throws() {
        Budget b = new Budget(USER_ID, "Negative",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                -100.00);
        service.createBudget(b);
    }

    // Business rule 2: unique name per user 

    @Test
    public void createBudget_withDuplicateName_sameUser_throws() {
        service.createBudget(validBudget("April Budget"));
        try {
            service.createBudget(validBudget("April Budget"));
            fail("Expected IllegalArgumentException for duplicate name");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().toLowerCase().contains("already"));
        }
    }

    @Test
    public void createBudget_withSameName_differentUser_succeeds() {
        Budget mine   = validBudget("Shared Name");
        Budget theirs = new Budget(2, "Shared Name",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                4000.00);

        service.createBudget(mine);
        service.createBudget(theirs);          // should NOT throw
        assertEquals(1, service.getBudgetsForUser(USER_ID).size());
        assertEquals(1, service.getBudgetsForUser(2).size());
    }

    // Update flow

    @Test
    public void updateBudget_withSameName_succeeds() {
        Budget b = validBudget("Editable");
        service.createBudget(b);

        b.setTotalIncome(5000.00);
        service.updateBudget(b);                // updating itself is fine

        assertEquals(5000.00,
                service.getBudgetById(b.getId()).getTotalIncome(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBudget_renameToExistingName_throws() {
        Budget a = validBudget("Budget A");
        Budget b = validBudget("Budget B");
        service.createBudget(a);
        service.createBudget(b);

        b.setName("Budget A");                  // collision with sibling
        service.updateBudget(b);
    }

    // Search / filter 

    @Test
    public void searchBudgets_byKeyword_filtersResults() {
        service.createBudget(validBudget("Spring Budget"));
        service.createBudget(validBudget("Summer Budget"));
        service.createBudget(validBudget("Vacation Plan"));

        List<Budget> spring = service.searchBudgets(USER_ID, "Spring", "ALL");
        assertEquals(1, spring.size());
        assertEquals("Spring Budget", spring.get(0).getName());
    }
}
