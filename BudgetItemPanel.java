package view;

import controller.BudgetItemController;
import model.Budget;
import model.BudgetItem;
import util.UIStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * A JDialog that shows all BudgetItems for a selected Budget.
 * Provides full CRUD and category filtering (the second search/filter option).
 */
public class BudgetItemPanel extends JDialog {

    private final BudgetItemController itemController;
    private final Budget budget;

    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JLabel summaryHeader;
    private JLabel summaryDetail;
    private JComboBox<String> categoryFilter;

    public BudgetItemPanel(JFrame parent, BudgetItemController itemController, Budget budget) {
        super(parent, "Expenses – " + budget.getName(), true);
        this.itemController = itemController;
        this.budget         = budget;

        setSize(820, 560);
        setLocationRelativeTo(parent);

        getContentPane().setBackground(UIStyle.BG_PAGE);
        setLayout(new BorderLayout());

        add(buildSummaryHeader(), BorderLayout.NORTH);
        add(buildCenter(),        BorderLayout.CENTER);

        refreshTable();
    }

    // Summary header (colored bar at top) 
    private JPanel buildSummaryHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.HEADER_BG);
        header.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        summaryHeader = new JLabel(budget.getName());
        summaryHeader.setFont(UIStyle.H2);
        summaryHeader.setForeground(Color.WHITE);

        summaryDetail = new JLabel(" ");
        summaryDetail.setFont(UIStyle.SMALL);
        summaryDetail.setForeground(new Color(255, 255, 255, 200));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        summaryHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryDetail.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(summaryHeader);
        stack.add(Box.createVerticalStrut(2));
        stack.add(summaryDetail);

        header.add(stack, BorderLayout.WEST);
        return header;
    }

    // Body: toolbar + table 
    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(UIStyle.BG_PAGE);
        center.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setBackground(UIStyle.BG_PAGE);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel catLbl = new JLabel("Filter by category:");
        catLbl.setFont(UIStyle.BODY_B);
        catLbl.setForeground(UIStyle.TEXT_DARK);

        categoryFilter = new JComboBox<>(buildCategoryOptions());
        categoryFilter.setFont(UIStyle.BODY);
        categoryFilter.setPreferredSize(new Dimension(160, 32));
        categoryFilter.addActionListener(e -> refreshTable());

        JButton addBtn    = UIStyle.successButton("+  Add Expense");
        JButton editBtn   = UIStyle.secondaryButton("Edit");
        JButton deleteBtn = UIStyle.dangerButton("Delete");

        toolbar.add(catLbl);
        toolbar.add(categoryFilter);
        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);

        center.add(toolbar);
        center.add(Box.createVerticalStrut(12));

        // Table
        center.add(buildTablePanel());

        // Wire actions
        addBtn.addActionListener(e -> {
            BudgetItemDialog dlg = new BudgetItemDialog(this, null, budget.getId());
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                try {
                    itemController.addItem(dlg.getItem(), budget.getTotalIncome());
                    refreshTable();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Cannot Add Expense", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        editBtn.addActionListener(e -> {
            int row = itemTable.getSelectedRow();
            if (row == -1) { warn("Please select an expense to edit."); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            BudgetItem existing = itemController.getItemById(id);
            BudgetItemDialog dlg = new BudgetItemDialog(this, existing, budget.getId());
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                try {
                    itemController.updateItem(dlg.getItem(), budget.getTotalIncome());
                    refreshTable();
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Cannot Update Expense", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            int row = itemTable.getSelectedRow();
            if (row == -1) { warn("Please select an expense to delete."); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this expense?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                itemController.deleteItem(id);
                refreshTable();
            }
        });

        return center;
    }

    private JComponent buildTablePanel() {
        String[] cols = {"ID", "Category", "Amount", "Description", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        itemTable = new JTable(tableModel);
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setRowHeight(32);
        itemTable.setFont(UIStyle.TABLE);
        itemTable.setForeground(UIStyle.TEXT_DARK);
        itemTable.setBackground(UIStyle.BG_CARD);
        itemTable.setShowGrid(false);
        itemTable.setShowHorizontalLines(true);
        itemTable.setGridColor(UIStyle.BORDER);
        itemTable.setSelectionBackground(new Color(0xEE, 0xEC, 0xFE));
        itemTable.setSelectionForeground(UIStyle.TEXT_DARK);
        itemTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader th = itemTable.getTableHeader();
        th.setFont(UIStyle.TABLE_HEAD);
        th.setBackground(UIStyle.TABLE_HEADER);
        th.setForeground(UIStyle.TEXT_DARK);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER));
        th.setPreferredSize(new Dimension(th.getPreferredSize().width, 36));

        DefaultTableCellRenderer altRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? UIStyle.BG_CARD : UIStyle.ROW_ALT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                int col = t.convertColumnIndexToModel(c);
                if (col == 2) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (col == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return comp;
            }
        };
        for (int i = 0; i < cols.length; i++) {
            itemTable.getColumnModel().getColumn(i).setCellRenderer(altRenderer);
        }
        itemTable.getColumnModel().getColumn(0).setMaxWidth(50);
        itemTable.getColumnModel().getColumn(0).setMinWidth(50);

        JScrollPane scroll = new JScrollPane(itemTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER, 1));
        scroll.getViewport().setBackground(UIStyle.BG_CARD);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        return scroll;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String selected = (String) categoryFilter.getSelectedItem();
        List<BudgetItem> items;

        if ("ALL".equals(selected)) {
            items = itemController.getItemsForBudget(budget.getId());
        } else {
            items = itemController.filterByCategory(budget.getId(), selected);
        }

        for (BudgetItem item : items) {
            tableModel.addRow(new Object[]{
                item.getId(),
                item.getCategory(),
                String.format("$%,.2f", item.getAmount()),
                item.getDescription(),
                item.getItemDate()
            });
        }

        // Update summary
        double allocated = itemController.getTotalAllocated(budget.getId());
        double remaining = budget.getTotalIncome() - allocated;
        summaryDetail.setText(String.format(
            "Income: $%,.2f   ·   Allocated: $%,.2f   ·   Remaining: $%,.2f",
            budget.getTotalIncome(), allocated, remaining));
    }

    private String[] buildCategoryOptions() {
        BudgetItem.Category[] cats = BudgetItem.Category.values();
        String[] opts = new String[cats.length + 1];
        opts[0] = "ALL";
        for (int i = 0; i < cats.length; i++) opts[i + 1] = cats[i].name();
        return opts;
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "No Selection", JOptionPane.WARNING_MESSAGE);
    }
}
