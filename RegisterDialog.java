package view;

import controller.UserController;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;

/** Registration dialog. Opened from the Login screen. */
public class RegisterDialog extends JDialog {

    private final UserController userController;
    private boolean registered = false;
    private String registeredUsername;

    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel statusLabel;

    public RegisterDialog(Dialog parent, UserController userController) {
        super(parent, "StackIt – Register", true);
        this.userController = userController;

        setSize(460, 620);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Page background 
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UIStyle.BG_PAGE);

        // Card 
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIStyle.BG_CARD);
        card.setBorder(BorderFactory.createLineBorder(UIStyle.BORDER, 1));
        card.setPreferredSize(new Dimension(380, 540));

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JLabel brand = new JLabel("Create your account");
        brand.setFont(UIStyle.H2);
        brand.setForeground(Color.WHITE);
        JLabel tag = new JLabel("Start tracking your budgets in seconds.");
        tag.setFont(UIStyle.SMALL);
        tag.setForeground(new Color(255, 255, 255, 200));
        JPanel brandStack = new JPanel();
        brandStack.setOpaque(false);
        brandStack.setLayout(new BoxLayout(brandStack, BoxLayout.Y_AXIS));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        tag.setAlignmentX(Component.LEFT_ALIGNMENT);
        brandStack.add(brand);
        brandStack.add(Box.createVerticalStrut(2));
        brandStack.add(tag);
        header.add(brandStack, BorderLayout.WEST);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(header);

        // Body 
        JPanel body = new JPanel();
        body.setBackground(UIStyle.BG_CARD);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(22, 30, 20, 30));

        body.add(buildField("Username",         usernameField = new JTextField()));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("Email",            emailField    = new JTextField()));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("Password",         passwordField = new JPasswordField()));
        body.add(Box.createVerticalStrut(12));
        body.add(buildField("Confirm Password", confirmField  = new JPasswordField()));
        body.add(Box.createVerticalStrut(8));

        // Status
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.SMALL);
        statusLabel.setForeground(UIStyle.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(statusLabel);
        body.add(Box.createVerticalStrut(14));

        // Buttons
        JButton backBtn     = UIStyle.secondaryButton("← Back");
        JButton registerBtn = UIStyle.primaryButton("Create Account");

        JPanel btnRow = new JPanel(new GridLayout(1, 2, 10, 0));
        btnRow.setBackground(UIStyle.BG_CARD);
        btnRow.add(backBtn);
        btnRow.add(registerBtn);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        body.add(btnRow);

        card.add(body);
        page.add(card);
        setContentPane(page);

        registerBtn.addActionListener(e -> attemptRegister());
        backBtn.addActionListener(e -> dispose());
    }

    /** Build a labeled field block (label on top, input below). */
    private JPanel buildField(String label, JTextField field) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setBackground(UIStyle.BG_CARD);
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

    private void attemptRegister() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        try {
            userController.register(username, email, password);
            registeredUsername = username;
            registered = true;
            JOptionPane.showMessageDialog(this,
                "Account created successfully! You can now log in.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Registration failed: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        statusLabel.setForeground(UIStyle.DANGER);
        statusLabel.setText(msg);
    }

    public boolean isRegistered()         { return registered; }
    public String getRegisteredUsername() { return registeredUsername; }
}
