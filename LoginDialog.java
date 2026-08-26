package view;

import controller.UserController;
import model.User;
import util.Session;
import util.UIStyle;

import javax.swing.*;
import java.awt.*;

/**
 * Login screen. Shown at application startup.
 * Provides a "Register" button to open RegisterDialog.
 *
 * Layout: a white card centered on a light-gray page background, with a
 * colored title bar at the top of the card and a soft drop-shadow effect.
 */
public class LoginDialog extends JDialog {

    private final UserController userController;
    private boolean loginSuccessful = false;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginDialog(UserController userController) {
        super((Frame) null, "StackIt – Login", true);
        this.userController = userController;

        setSize(440, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        // Page background 
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(UIStyle.BG_PAGE);

        // White card 
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIStyle.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIStyle.BORDER, 1),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        card.setPreferredSize(new Dimension(360, 460));

        // Header bar inside card 
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIStyle.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        JLabel brand = new JLabel("StackIt");
        brand.setFont(UIStyle.H1);
        brand.setForeground(Color.WHITE);
        JLabel tag = new JLabel("Smart budgeting, simplified.");
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
        body.setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));
        body.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(UIStyle.H2);
        welcome.setForeground(UIStyle.TEXT_DARK);
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to continue to your dashboard");
        sub.setFont(UIStyle.SMALL);
        sub.setForeground(UIStyle.TEXT_MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(welcome);
        body.add(Box.createVerticalStrut(4));
        body.add(sub);
        body.add(Box.createVerticalStrut(22));

        // Username
        JLabel userLbl = UIStyle.formLabel("Username");
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField = new JTextField();
        usernameField.setFont(UIStyle.BODY);
        usernameField.setBorder(UIStyle.fieldBorder());
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password
        JLabel passLbl = UIStyle.formLabel("Password");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = new JPasswordField();
        passwordField.setFont(UIStyle.BODY);
        passwordField.setBorder(UIStyle.fieldBorder());
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(userLbl);
        body.add(Box.createVerticalStrut(6));
        body.add(usernameField);
        body.add(Box.createVerticalStrut(14));
        body.add(passLbl);
        body.add(Box.createVerticalStrut(6));
        body.add(passwordField);
        body.add(Box.createVerticalStrut(8));

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIStyle.SMALL);
        statusLabel.setForeground(UIStyle.DANGER);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(statusLabel);
        body.add(Box.createVerticalStrut(10));

        // Login button
        JButton loginBtn = UIStyle.primaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(loginBtn);
        body.add(Box.createVerticalStrut(18));

        // Divider
        JSeparator divider = new JSeparator();
        divider.setForeground(UIStyle.BORDER);
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(divider);
        body.add(Box.createVerticalStrut(14));

        // No account row
        JPanel noAccountRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        noAccountRow.setBackground(UIStyle.BG_CARD);
        noAccountRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        noAccountRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        JLabel noAcct = new JLabel("Don't have an account?");
        noAcct.setFont(UIStyle.BODY);
        noAcct.setForeground(UIStyle.TEXT_MUTED);
        JButton registerBtn = UIStyle.secondaryButton("Create Account");
        noAccountRow.add(noAcct);
        noAccountRow.add(registerBtn);
        body.add(noAccountRow);

        card.add(body);
        page.add(card);
        setContentPane(page);

        // Actions
        loginBtn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());

        registerBtn.addActionListener(e -> {
            RegisterDialog rd = new RegisterDialog(this, userController);
            rd.setVisible(true);
            if (rd.isRegistered()) {
                usernameField.setText(rd.getRegisteredUsername());
                statusLabel.setForeground(UIStyle.SUCCESS);
                statusLabel.setText("Account created. Please log in.");
            }
        });
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        User user = userController.login(username, password);
        if (user != null) {
            Session.setCurrentUser(user);
            loginSuccessful = true;
            dispose();
        } else {
            showError("Invalid username or password.");
            passwordField.setText("");
        }
    }

    private void showError(String msg) {
        statusLabel.setForeground(UIStyle.DANGER);
        statusLabel.setText(msg);
    }

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}
