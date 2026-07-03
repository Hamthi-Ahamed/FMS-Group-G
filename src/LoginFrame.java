import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class LoginFrame extends JFrame implements ActionListener {
    private static final int FRAME_WIDTH = 940;
    private static final int FRAME_HEIGHT = 560;

    // Right-side form fields
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    // Labels that move in Sign Up mode
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel confirmPasswordLabel;
    private JLabel roleLabel;

    // true = Sign Up mode
    private boolean signUpMode = false;

    // Role selector buttons
    private JButton adminButton;
    private JButton studentButton;
    private JButton lecturerButton;
    private String selectedRole = "Admin";

    // Tabs
    private JButton signInTab;
    private JButton signUpTab;
    private JPanel underline;


    private JButton signInButton;

    private final Color THEME_PURPLE = new Color(98, 60, 234);
    private final Color LIGHT_GRAY = new Color(225, 225, 225);

    public LoginFrame() {
        setTitle("Faculty Management System - Login");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildLeftPanel(), BorderLayout.WEST);
        getContentPane().add(buildRightPanel(), BorderLayout.CENTER);
    }


    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(null);
        p.setPreferredSize(new Dimension(370, FRAME_HEIGHT));
        p.setBackground(THEME_PURPLE);

        JLabel cap = new JLabel("🎓", SwingConstants.CENTER);
        cap.setFont(new Font("SansSerif", Font.PLAIN, 80));
        cap.setForeground(Color.WHITE);
        cap.setBounds(0, 80, 370, 110);
        p.add(cap);

        JLabel title1 = new JLabel("Faculty Management", SwingConstants.CENTER);
        title1.setFont(new Font("Arial", Font.BOLD, 26));
        title1.setForeground(Color.WHITE);
        title1.setBounds(15, 190, 340, 35);
        p.add(title1);

        JLabel title2 = new JLabel("System", SwingConstants.CENTER);
        title2.setFont(new Font("Arial", Font.BOLD, 26));
        title2.setForeground(Color.WHITE);
        title2.setBounds(15, 225, 340, 35);
        p.add(title2);

        JLabel faculty = new JLabel("Faculty of Computing & Technology", SwingConstants.CENTER);
        faculty.setFont(new Font("Arial", Font.BOLD, 15));
        faculty.setForeground(Color.WHITE);
        faculty.setBounds(15, 380, 340, 25);
        p.add(faculty);

        JLabel sub = new JLabel("Manage your academic journey", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 13));
        sub.setForeground(new Color(230, 225, 250));
        sub.setBounds(15, 410, 340, 20);
        p.add(sub);

        return p;
    }


    private JPanel buildRightPanel() {
        JPanel p = new JPanel(null);
        p.setPreferredSize(new Dimension(FRAME_WIDTH - 370, FRAME_HEIGHT));
        p.setBackground(Color.WHITE);


        signInTab = new JButton("Sign In");
        signInTab.setFont(new Font("Arial", Font.BOLD, 18));
        signInTab.setForeground(THEME_PURPLE);
        signInTab.setBorderPainted(false);
        signInTab.setContentAreaFilled(false);
        signInTab.setFocusPainted(false);
        signInTab.setBounds(60, 30, 110, 30);
        signInTab.addActionListener(this);
        p.add(signInTab);

        signUpTab = new JButton("Sign Up");
        signUpTab.setFont(new Font("Arial", Font.BOLD, 18));
        signUpTab.setForeground(Color.GRAY);
        signUpTab.setBorderPainted(false);
        signUpTab.setContentAreaFilled(false);
        signUpTab.setFocusPainted(false);
        signUpTab.setBounds(220, 30, 110, 30);
        signUpTab.addActionListener(this);
        p.add(signUpTab);

        underline = new JPanel();
        underline.setBackground(THEME_PURPLE);
        underline.setBounds(60, 62, 110, 3);
        p.add(underline);


        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        usernameLabel.setForeground(THEME_PURPLE);
        usernameLabel.setBounds(60, 90, 200, 25);
        p.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 15));
        usernameField.setBounds(60, 118, 450, 40);
        usernameField.setBorder(BorderFactory.createLineBorder(THEME_PURPLE, 2, true));
        p.add(usernameField);


        passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 15));
        passwordLabel.setForeground(THEME_PURPLE);
        passwordLabel.setBounds(60, 175, 200, 25);
        p.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 15));
        passwordField.setBounds(60, 203, 450, 40);
        passwordField.setEchoChar('*');
        passwordField.setBorder(BorderFactory.createLineBorder(THEME_PURPLE, 2, true));
        p.add(passwordField);


        confirmPasswordLabel = new JLabel("Confirm Password");
        confirmPasswordLabel.setFont(new Font("Arial", Font.BOLD, 15));
        confirmPasswordLabel.setForeground(THEME_PURPLE);
        confirmPasswordLabel.setBounds(60, 260, 250, 25);
        confirmPasswordLabel.setVisible(false);
        p.add(confirmPasswordLabel);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 15));
        confirmPasswordField.setBounds(60, 288, 450, 40);
        confirmPasswordField.setEchoChar('*');
        confirmPasswordField.setBorder(BorderFactory.createLineBorder(THEME_PURPLE, 2, true));
        confirmPasswordField.setVisible(false);
        p.add(confirmPasswordField);


        roleLabel = new JLabel("Role");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        roleLabel.setForeground(THEME_PURPLE);
        roleLabel.setBounds(60, 260, 200, 25);
        p.add(roleLabel);

        adminButton = new JButton("Admin");
        styleRoleButton(adminButton, true);
        adminButton.setBounds(60, 288, 100, 35);
        adminButton.addActionListener(this);
        p.add(adminButton);

        studentButton = new JButton("Student");
        styleRoleButton(studentButton, false);
        studentButton.setBounds(175, 288, 100, 35);
        studentButton.addActionListener(this);
        p.add(studentButton);

        lecturerButton = new JButton("Lecturer");
        styleRoleButton(lecturerButton, false);
        lecturerButton.setBounds(290, 288, 100, 35);
        lecturerButton.addActionListener(this);
        p.add(lecturerButton);


        signInButton = new JButton("Sign In");
        signInButton.setFont(new Font("Arial", Font.BOLD, 17));
        signInButton.setForeground(Color.WHITE);
        signInButton.setBackground(THEME_PURPLE);
        signInButton.setFocusPainted(false);
        signInButton.setBounds(60, 350, 450, 45);
        signInButton.addActionListener(this);
        p.add(signInButton);

        applyFormLayout();
        return p;
    }

    private void styleRoleButton(JButton btn, boolean selected) {
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        if (selected) {
            btn.setBackground(THEME_PURPLE);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(LIGHT_GRAY);
            btn.setForeground(Color.GRAY);
        }
    }

    private void applyFormLayout() {
        if (signUpMode) {
            confirmPasswordLabel.setVisible(true);
            confirmPasswordField.setVisible(true);

            roleLabel.setBounds(60, 345, 200, 25);
            adminButton.setBounds(60, 373, 100, 35);
            studentButton.setBounds(175, 373, 100, 35);
            lecturerButton.setBounds(290, 373, 100, 35);

            signInButton.setText("Sign Up");
            signInButton.setBounds(60, 435, 450, 45);

            signInTab.setForeground(Color.GRAY);
            signUpTab.setForeground(THEME_PURPLE);
            underline.setBounds(220, 62, 110, 3);
        } else {
            confirmPasswordLabel.setVisible(false);
            confirmPasswordField.setVisible(false);

            roleLabel.setBounds(60, 260, 200, 25);
            adminButton.setBounds(60, 288, 100, 35);
            studentButton.setBounds(175, 288, 100, 35);
            lecturerButton.setBounds(290, 288, 100, 35);

            signInButton.setText("Sign In");
            signInButton.setBounds(60, 350, 450, 45);

            signInTab.setForeground(THEME_PURPLE);
            signUpTab.setForeground(Color.GRAY);
            underline.setBounds(60, 62, 110, 3);
        }
        revalidate();
        repaint();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();


        if (src == adminButton) {
            selectedRole = "Admin";
            styleRoleButton(adminButton, true);
            styleRoleButton(studentButton, false);
            styleRoleButton(lecturerButton, false);
        } else if (src == studentButton) {
            selectedRole = "Student";
            styleRoleButton(adminButton, false);
            styleRoleButton(studentButton, true);
            styleRoleButton(lecturerButton, false);
        } else if (src == lecturerButton) {
            selectedRole = "Lecturer";
            styleRoleButton(adminButton, false);
            styleRoleButton(studentButton, false);
            styleRoleButton(lecturerButton, true);
        } else if (src == signUpTab) {
            signUpMode = true;
            applyFormLayout();
        } else if (src == signInTab) {
            signUpMode = false;
            applyFormLayout();
        } else if (src == signInButton) {
            if (signUpMode) {
                handleSignUp();
            } else {
                handleSignIn();
            }
        }
    }


    private void handleSignIn() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.");
            return;
        }

        String roleFromDB = DBConnector.verifyLogin(username, password);
        if (roleFromDB == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!roleFromDB.equalsIgnoreCase(selectedRole)) {
            JOptionPane.showMessageDialog(this,
                    "You selected role \"" + selectedRole + "\" but your account role is \"" + roleFromDB + "\".\n" +
                            "Please choose the correct role.", "Role Mismatch",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        dispose();
        switch (roleFromDB) {
            case "Student":
                new StudentDashboard(username).setVisible(true);
                break;
            case "Lecturer":
                JOptionPane.showMessageDialog(null, "Lecturer dashboard coming soon.");
                break;
            case "Admin":
                new AdminStudentsPage(username).setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(null, "Unknown role: " + roleFromDB);
        }
    }


    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }

        boolean success = DBConnector.registerUser(username, password, selectedRole);
        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Account created successfully for \"" + username + "\" as " + selectedRole + ".\n" +
                            "You can now Sign In.");
            signUpMode = false;
            applyFormLayout();
            usernameField.setText(username);
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Username \"" + username + "\" already exists. Please choose a different username.",
                    "Registration Failed", JOptionPane.ERROR_MESSAGE);
        }
    }


     public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
     }
}
