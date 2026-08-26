package main;

import controller.BudgetController;
import controller.BudgetItemController;
import controller.UserController;
import dao.BudgetDao;
import dao.BudgetItemDao;
import dao.JdbcBudgetDao;
import dao.JdbcBudgetItemDao;
import dao.JdbcUserDao;
import dao.UserDao;
import model.Budget;
import model.BudgetItem;
import model.User;
import service.BudgetItemService;
import service.BudgetItemServiceImplementation;
import service.BudgetService;
import service.BudgetServiceImplementation;
import service.UserService;
import service.UserServiceImplementation;
import view.LoginDialog;
import view.MainWindow;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.time.LocalDate;

public class App {

    private static UserController       userController;
    private static BudgetController     budgetController;
    private static BudgetItemController itemController;

    public static void main(String[] args) {

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        UserDao       userDAO   = new JdbcUserDao();
        BudgetItemDao itemDAO   = new JdbcBudgetItemDao();
        BudgetDao     budgetDAO = new JdbcBudgetDao();

        UserService       userService   = new UserServiceImplementation(userDAO);
        BudgetService     budgetService = new BudgetServiceImplementation(budgetDAO);
        BudgetItemService itemService   = new BudgetItemServiceImplementation(itemDAO);

        userController   = new UserController(userService);
        budgetController = new BudgetController(budgetService);
        itemController   = new BudgetItemController(itemService);

        seedIfEmpty(userService, budgetService, itemService, userDAO);

        SwingUtilities.invokeLater(App::showLogin);
    }

    public static void showLogin() {
        LoginDialog login = new LoginDialog(userController);
        login.setVisible(true);

        if (login.isLoginSuccessful()) {
            SwingUtilities.invokeLater(() -> {
                MainWindow window = new MainWindow(
                        budgetController,
                        itemController,
                        () -> SwingUtilities.invokeLater(App::showLogin)
                );
                window.setVisible(true);
            });
        } else {
            System.exit(0);
        }
    }

    private static void seedIfEmpty(UserService userService,
                                    BudgetService budgetService,
                                    BudgetItemService itemService,
                                    UserDao userDAO) {
        try {
            if (userDAO.findByUsername("demo") != null) {
                return;
            }

            User demo = userService.register("demo", "demo@stackit.app", "demo123");
            int uid = demo.getId();

            Budget april = new Budget(uid, "April Budget",
                    LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 30), 3500.00);
            budgetService.createBudget(april);

            itemService.addItem(new BudgetItem(april.getId(), uid,
                    BudgetItem.Category.HOUSING, 1200.00, "Monthly rent",
                    LocalDate.of(2025, 4, 1)), april.getTotalIncome());
            itemService.addItem(new BudgetItem(april.getId(), uid,
                    BudgetItem.Category.FOOD, 320.50, "Groceries + dining",
                    LocalDate.of(2025, 4, 10)), april.getTotalIncome());
            itemService.addItem(new BudgetItem(april.getId(), uid,
                    BudgetItem.Category.TRANSPORT, 85.00, "Monthly bus pass",
                    LocalDate.of(2025, 4, 3)), april.getTotalIncome());
            itemService.addItem(new BudgetItem(april.getId(), uid,
                    BudgetItem.Category.ENTERTAINMENT, 45.99, "Streaming",
                    LocalDate.of(2025, 4, 5)), april.getTotalIncome());
            itemService.addItem(new BudgetItem(april.getId(), uid,
                    BudgetItem.Category.HEALTHCARE, 30.00, "Gym membership",
                    LocalDate.of(2025, 4, 7)), april.getTotalIncome());

            Budget may = new Budget(uid, "May Budget",
                    LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31), 3500.00);
            budgetService.createBudget(may);
            itemService.addItem(new BudgetItem(may.getId(), uid,
                    BudgetItem.Category.HOUSING, 1200.00, "Monthly rent",
                    LocalDate.of(2025, 5, 1)), may.getTotalIncome());
            itemService.addItem(new BudgetItem(may.getId(), uid,
                    BudgetItem.Category.FOOD, 275.00, "Weekly groceries",
                    LocalDate.of(2025, 5, 8)), may.getTotalIncome());
            itemService.addItem(new BudgetItem(may.getId(), uid,
                    BudgetItem.Category.EDUCATION, 199.00, "Online course",
                    LocalDate.of(2025, 5, 12)), may.getTotalIncome());

            Budget vacation = new Budget(uid, "Summer Vacation Fund",
                    LocalDate.of(2025, 6, 1), LocalDate.of(2025, 8, 31), 2000.00);
            budgetService.createBudget(vacation);
            vacation.setStatus(Budget.Status.ARCHIVED);
            budgetService.updateBudget(vacation);
            itemService.addItem(new BudgetItem(vacation.getId(), uid,
                    BudgetItem.Category.TRANSPORT, 450.00, "Flight tickets",
                    LocalDate.of(2025, 6, 15)), vacation.getTotalIncome());
            itemService.addItem(new BudgetItem(vacation.getId(), uid,
                    BudgetItem.Category.HOUSING, 780.00, "Hotel 3 nights",
                    LocalDate.of(2025, 7, 20)), vacation.getTotalIncome());

            Budget savings = new Budget(uid, "Emergency Savings",
                    LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), 5000.00);
            budgetService.createBudget(savings);
            itemService.addItem(new BudgetItem(savings.getId(), uid,
                    BudgetItem.Category.SAVINGS, 500.00, "January deposit",
                    LocalDate.of(2025, 1, 31)), savings.getTotalIncome());
            itemService.addItem(new BudgetItem(savings.getId(), uid,
                    BudgetItem.Category.SAVINGS, 500.00, "February deposit",
                    LocalDate.of(2025, 2, 28)), savings.getTotalIncome());
            itemService.addItem(new BudgetItem(savings.getId(), uid,
                    BudgetItem.Category.SAVINGS, 500.00, "March deposit",
                    LocalDate.of(2025, 3, 31)), savings.getTotalIncome());

            System.out.println("[StackIt] First run – seeded demo data.");
        } catch (Exception ex) {
            System.err.println("[StackIt] Seeding skipped: " + ex.getMessage());
        }
    }
}