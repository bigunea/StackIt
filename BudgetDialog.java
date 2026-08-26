package view;

import model.Budget;
import util.Session;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Create / Edit dialog for a Budget. */
public class BudgetDialog extends JDialog {

    private JTextField nameField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField totalIncomeField;
    private JComboBox<Budget.Status> statusComboBox;

    private boolean confirmed = false;
    private Budget budget;

    public BudgetDialog(JFrame parent, Budget existingBudget) {
        super(parent, true);
        this.budget = existingBudget;

        boolean editing = existingBudget != null;
        setTitle(editing ? "Edit Budget" : "New Budget");
        setSize(480, 540);
        setLocationRelativeTo(parent);
        setResizable(false);

        getContentPane().setBackground(UIStyle.BG_PAGE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        JLabel title = new JLabel(editing ? "Edit Budget" : "Create New Budget");
        title.setFont(UIStyle.H2);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel(editing
                ? "Update the details for this budget"
                : "Set up a new monthly or custom budget period");
        sub.setFont(UIStyle.SMALL);
        sub.setForeground(new Color(255, 255, 255, 200));
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(title);
        stack.add(Box.createVerticalStrut(2));
        stack.add(sub);
        header.add(stack, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UIStyle.BG_PAGE);
        body.setBorder(BorderFactory.createEmptyBorder(22, 30, 16, 30));

        nameField        = new JTextField();
        startDateField   = new JTextField(LocalDate.now().toString());
        endDateField     = new JTextField(LocalDate.now().plusMonths(1).toString());
        totalIncomeField = new JTextField();
        statusComboBox   = new JComboBox<>(Budget.Status.values());
        statusComboBox.setFont(UIStyle.BODY);

        body.add(buildField("Budget Name",           nameField));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("Start Date (YYYY-MM-DD)", startDateField));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("End Date (YYYY-MM-DD)",   endDateField));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("Total Income ($)",      totalIncomeField));
        body.add(Box.createVerticalStrut(12));
        body.add(buildComboField("Status",           statusComboBox));

        if (editing) {
            nameField.setText(existingBudget.getName());
            startDateField.setText(existingBudget.getStartDate().toString());
            endDateField.setText(existingBudget.getEndDate().toString());
            totalIncomeField.setText(String.valueOf(existingBudget.getTotalIncome()));
            statusComboBox.setSelectedItem(existingBudget.getStatus());
        }

        add(body, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(UIStyle.BG_PAGE);
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyle.BORDER),
            BorderFactory.createEmptyBorder(8, 22, 8, 22)));
        JButton cancelBtn = UIStyle.secondaryButton("Cancel");
        JButton okBtn     = UIStyle.primaryButton(editing ? "Save Changes" : "Create Budget");
        btnPanel.add(cancelBtn);
        btnPanel.add(okBtn);
        add(btnPanel, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            if (validateAndBuild()) {
                confirmed = true;
                dispose();
            }
        });
        cancelBtn.addActionListener(e -> dispose());
    }

    private JPanel buildField(String label, JTextField field) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(UIStyle.BG_PAGE);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = UIStyle.formLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(UIStyle.BODY);
        field.setBorder(UIStyle.fieldBorder());
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(l);
        block.add(Box.createVerticalStrut(6));
        block.add(field);
        return block;
    }

    private JPanel buildComboField(String label, JComboBox<?> combo) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(UIStyle.BG_PAGE);
        block.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = UIStyle.formLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);

        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);

        block.add(l);
        block.add(Box.createVerticalStrut(6));
        block.add(combo);
        return block;
    }

    private boolean validateAndBuild() {
        String name = nameField.getText().trim();

        if (name.isBlank()) {
            error("Budget name is required.");
            return false;
        }
        if (!name.matches("[a-zA-Z0-9 \\-_']+")) {
            error("Name may only contain letters, numbers, spaces, hyphens, or apostrophes.");
            return false;
        }

        LocalDate startDate, endDate;
        try {
            startDate = LocalDate.parse(startDateField.getText().trim());
            endDate   = LocalDate.parse(endDateField.getText().trim());
        } catch (DateTimeParseException ex) {
            error("Dates must be in YYYY-MM-DD format (e.g. 2025-05-01).");
            return false;
        }

        double totalIncome;
        try {
            totalIncome = Double.parseDouble(totalIncomeField.getText().trim());
        } catch (NumberFormatException ex) {
            error("Total income must be a valid number.");
            return false;
        }

        Budget.Status status = (Budget.Status) statusComboBox.getSelectedItem();
        int userId = Session.getCurrentUser().getId();

        if (budget == null) {
            budget = new Budget(userId, name, startDate, endDate, totalIncome);
        } else {
            budget.setName(name);
            budget.setStartDate(startDate);
            budget.setEndDate(endDate);
            budget.setTotalIncome(totalIncome);
            budget.setStatus(status);
        }
        return true;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }
    public Budget getBudget()    { return budget; }
}
