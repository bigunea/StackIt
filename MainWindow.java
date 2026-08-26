package view;

import controller.BudgetController;
import controller.BudgetItemController;
import model.Budget;
import util.Session;
import util.UIStyle;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;

/**
 * Main application window. Shows the current user's budgets in a styled table.
 *
 * SwingWorker usage: budget data is loaded on a background thread so the UI
 * stays responsive. A "Loading…" status message is shown while the query runs.
 *
 * Search / filter options:
 *  1. Name keyword (text field)
 *  2. Status dropdown (ALL / ACTIVE / ARCHIVED)
 */
public class MainWindow extends JFrame {

    private final BudgetController     budgetController;
    private final BudgetItemController itemController;
    private final Runnable             logoutCallback;

    private JTable budgetTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    private JTextField searchField;
    private JComboBox<String> statusFilter;

    public MainWindow(BudgetController budgetController,
                      BudgetItemController itemController,
                      Runnable logoutCallback) {
        this.budgetController = budgetController;
        this.itemController   = itemController;
        this.logoutCallback   = logoutCallback;

        setTitle("StackIt – Budget Tracker");
        setSize(960, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(UIStyle.BG_PAGE);
        setLayout(new BorderLayout());

        add(buildHeaderBar(),  BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildStatusBar(),  BorderLayout.SOUTH);

        refreshTable();   // loads via SwingWorker
    }

    //  Top header bar (dark) 
    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIStyle.HEADER_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        // Brand
        JLabel brand = new JLabel("StackIt");
        brand.setFont(UIStyle.H2);
        brand.setForeground(Color.WHITE);

        JLabel sub = new JLabel("  Budget Tracker");
        sub.setFont(UIStyle.SMALL);
        sub.setForeground(new Color(255, 255, 255, 160));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(brand);
        left.add(sub);

        // User info + logout
        JLabel user = new JLabel("Signed in as " +
                Session.getCurrentUser().getUsername());
        user.setFont(UIStyle.SMALL);
        user.setForeground(new Color(255, 255, 255, 200));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(UIStyle.DANGER);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setFont(UIStyle.BTN);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> logout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);
        right.add(user);
        right.add(logoutBtn);

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    //  Center area: action toolbar + search + table 
    private JPanel buildCenterPanel() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(UIStyle.BG_PAGE);
        center.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        // Page title
        JLabel pageTitle = new JLabel("My Budgets");
        pageTitle.setFont(UIStyle.H1);
        pageTitle.setForeground(UIStyle.TEXT_DARK);
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pageSub = new JLabel("Create, manage, and track your monthly budgets");
        pageSub.setFont(UIStyle.BODY);
        pageSub.setForeground(UIStyle.TEXT_MUTED);
        pageSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(pageTitle);
        center.add(Box.createVerticalStrut(2));
        center.add(pageSub);
        center.add(Box.createVerticalStrut(16));

        // Action toolbar
        JPanel toolbar = buildActionToolbar();
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(toolbar);
        center.add(Box.createVerticalStrut(12));

        // Search row
        JPanel search = buildSearchRow();
        search.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(search);
        center.add(Box.createVerticalStrut(12));

        // Table
        JComponent table = buildTablePanel();
        table.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(table);

        return center;
    }

    private JPanel buildActionToolbar() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(UIStyle.BG_PAGE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton newBudgetBtn = UIStyle.primaryButton("+  New Budget");
        JButton editBtn      = UIStyle.secondaryButton("Edit");
        JButton deleteBtn    = UIStyle.dangerButton("Delete");
        JButton itemsBtn     = UIStyle.accentButton("Manage Expenses");
        JButton refreshBtn   = UIStyle.secondaryButton("↻  Refresh");

        row.add(newBudgetBtn);
        row.add(editBtn);
        row.add(deleteBtn);
        row.add(itemsBtn);
        row.add(refreshBtn);

        // Wire actions
        refreshBtn.addActionListener(e -> refreshTable());

        newBudgetBtn.addActionListener(e -> {
            BudgetDialog dlg = new BudgetDialog(this, null);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                try {
                    budgetController.createBudget(dlg.getBudget());
                    refreshTable();
                    setStatus("Budget created successfully.");
                } catch (IllegalArgumentException ex) {
                    showError(ex.getMessage());
                }
            }
        });

        editBtn.addActionListener(e -> {
            Budget selected = getSelectedBudget();
            if (selected == null) return;
            BudgetDialog dlg = new BudgetDialog(this, selected);
            dlg.setVisible(true);
            if (dlg.isConfirmed()) {
                try {
                    budgetController.updateBudget(dlg.getBudget());
                    refreshTable();
                    setStatus("Budget updated.");
                } catch (IllegalArgumentException ex) {
                    showError(ex.getMessage());
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            Budget selected = getSelectedBudget();
            if (selected == null) return;
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getName() + "\" and all its expenses?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                budgetController.deleteBudget(selected.getId());
                refreshTable();
                setStatus("Budget deleted.");
            }
        });

        itemsBtn.addActionListener(e -> {
            Budget selected = getSelectedBudget();
            if (selected == null) return;
            BudgetItemPanel panel =
                new BudgetItemPanel(this, itemController, selected);
            panel.setVisible(true);
            refreshTable();
        });

        return row;
    }

    private JPanel buildSearchRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setBackground(UIStyle.BG_PAGE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel sLbl = new JLabel("Search:");
        sLbl.setFont(UIStyle.BODY_B);
        sLbl.setForeground(UIStyle.TEXT_DARK);

        searchField = new JTextField(18);
        searchField.setFont(UIStyle.BODY);
        searchField.setBorder(UIStyle.fieldBorder());
        searchField.setPreferredSize(new Dimension(220, 32));

        JLabel fLbl = new JLabel("Status:");
        fLbl.setFont(UIStyle.BODY_B);
        fLbl.setForeground(UIStyle.TEXT_DARK);

        statusFilter = new JComboBox<>(new String[]{"ALL", "ACTIVE", "ARCHIVED"});
        statusFilter.setFont(UIStyle.BODY);
        statusFilter.setPreferredSize(new Dimension(120, 32));

        JButton searchBtn = UIStyle.primaryButton("Search");
        JButton clearBtn  = UIStyle.secondaryButton("Clear");

        row.add(sLbl);
        row.add(searchField);
        row.add(Box.createHorizontalStrut(10));
        row.add(fLbl);
        row.add(statusFilter);
        row.add(searchBtn);
        row.add(clearBtn);

        searchBtn.addActionListener(e -> refreshTable());
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilter.setSelectedIndex(0);
            refreshTable();
        });
        searchField.addActionListener(e -> refreshTable());

        return row;
    }

    private JComponent buildTablePanel() {
        String[] cols = {"ID", "Name", "Start Date", "End Date",
                         "Income", "Allocated", "Remaining", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        budgetTable = new JTable(tableModel);
        budgetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        budgetTable.setRowHeight(32);
        budgetTable.setFont(UIStyle.TABLE);
        budgetTable.setForeground(UIStyle.TEXT_DARK);
        budgetTable.setBackground(UIStyle.BG_CARD);
        budgetTable.setShowGrid(false);
        budgetTable.setShowHorizontalLines(true);
        budgetTable.setGridColor(UIStyle.BORDER);
        budgetTable.setSelectionBackground(new Color(0xEE, 0xEC, 0xFE));
        budgetTable.setSelectionForeground(UIStyle.TEXT_DARK);
        budgetTable.setIntercellSpacing(new Dimension(0, 1));

        // Header
        JTableHeader th = budgetTable.getTableHeader();
        th.setFont(UIStyle.TABLE_HEAD);
        th.setBackground(UIStyle.TABLE_HEADER);
        th.setForeground(UIStyle.TEXT_DARK);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyle.BORDER));
        th.setPreferredSize(new Dimension(th.getPreferredSize().width, 36));

        // Alternating row renderer + currency right-align
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
                if (col == 4 || col == 5 || col == 6) {
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
            budgetTable.getColumnModel().getColumn(i).setCellRenderer(altRenderer);
        }
        budgetTable.getColumnModel().getColumn(0).setMaxWidth(50);
        budgetTable.getColumnModel().getColumn(0).setMinWidth(50);

        JScrollPane scroll = new JScrollPane(budgetTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER, 1));
        scroll.getViewport().setBackground(UIStyle.BG_CARD);
        return scroll;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UIStyle.BG_CARD);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, UIStyle.BORDER),
            BorderFactory.createEmptyBorder(8, 22, 8, 22)));
        statusLabel = new JLabel("Welcome to StackIt, "
                + Session.getCurrentUser().getUsername() + "!");
        statusLabel.setFont(UIStyle.SMALL);
        statusLabel.setForeground(UIStyle.TEXT_MUTED);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    // SwingWorker-based table refresh 
    private void refreshTable() {
        String keyword = searchField.getText().trim();
        String status  = (String) statusFilter.getSelectedItem();
        setStatus("Loading…");

        SwingWorker<List<Budget>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Budget> doInBackground() throws Exception {
                Thread.sleep(2000);  // TEMPORARY — for screenshot only
                if (keyword.isEmpty() && "ALL".equals(status)) {
                    return budgetController.getBudgetsForCurrentUser();
                }
                return budgetController.searchBudgets(keyword, status);
            }

            @Override
            protected void done() {
                try {
                    List<Budget> budgets = get();
                    tableModel.setRowCount(0);
                    for (Budget b : budgets) {
                        tableModel.addRow(new Object[]{
                            b.getId(),
                            b.getName(),
                            b.getStartDate(),
                            b.getEndDate(),
                            String.format("$%,.2f", b.getTotalIncome()),
                            String.format("$%,.2f", b.getTotalAllocated()),
                            String.format("$%,.2f", b.getRemaining()),
                            b.getStatus()
                        });
                    }
                    setStatus("Showing " + budgets.size() + " budget(s).");
                } catch (Exception ex) {
                    setStatus("Error loading budgets: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // Helpers 
    private Budget getSelectedBudget() {
        int row = budgetTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a budget first.",
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        return budgetController.getBudgetById(id);
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void logout() {
        Session.logout();
        dispose();
        logoutCallback.run();
    }
}
