package view;

import model.BudgetItem;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Add / Edit dialog for a BudgetItem. */
public class BudgetItemDialog extends JDialog {

    private JComboBox<BudgetItem.Category> categoryCombo;
    private JTextField amountField;
    private JTextField descriptionField;
    private JTextField dateField;

    private boolean confirmed = false;
    private BudgetItem item;
    private final int budgetId;

    public BudgetItemDialog(JDialog parent, BudgetItem existing, int budgetId) {
        super(parent, existing == null ? "Add Expense" : "Edit Expense", true);
        this.item     = existing;
        this.budgetId = budgetId;

        boolean editing = existing != null;
        setSize(440, 480);
        setLocationRelativeTo(parent);
        setResizable(false);

        getContentPane().setBackground(UIStyle.BG_PAGE);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.SUCCESS);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
        JLabel title = new JLabel(editing ? "Edit Expense" : "Add New Expense");
        title.setFont(UIStyle.H2);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel(editing
                ? "Update the details of this expense"
                : "Record an expense against this budget");
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

        categoryCombo    = new JComboBox<>(BudgetItem.Category.values());
        categoryCombo.setFont(UIStyle.BODY);
        amountField      = new JTextField();
        descriptionField = new JTextField();
        dateField        = new JTextField(LocalDate.now().toString());

        body.add(buildComboField("Category", categoryCombo));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("Amount ($)",          amountField));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("Description",         descriptionField));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("Date (YYYY-MM-DD)",   dateField));

        if (editing) {
            categoryCombo.setSelectedItem(existing.getCategory());
            amountField.setText(String.valueOf(existing.getAmount()));
            descriptionField.setText(existing.getDescription());
            dateField.setText(existing.getItemDate().toString());
        }
        add(body, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(UIStyle.BG_PAGE);
        btnPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyle.BORDER),
            BorderFactory.createEmptyBorder(8, 22, 8, 22)));
        JButton cancelBtn = UIStyle.secondaryButton("Cancel");
        JButton saveBtn   = UIStyle.successButton(editing ? "Save Changes" : "Add Expense");
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
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
        BudgetItem.Category category = (BudgetItem.Category) categoryCombo.getSelectedItem();

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            error("Amount must be a positive number.");
            return false;
        }

        String desc = descriptionField.getText().trim();
        if (desc.isBlank()) {
            error("Description is required.");
            return false;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim());
        } catch (DateTimeParseException ex) {
            error("Date must be in YYYY-MM-DD format.");
            return false;
        }

        if (item == null) {
            item = new BudgetItem(budgetId, 0, category, amount, desc, date);
        } else {
            item.setCategory(category);
            item.setAmount(amount);
            item.setDescription(desc);
            item.setItemDate(date);
        }
        return true;
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() { return confirmed; }
    public BudgetItem getItem()  { return item; }
}
