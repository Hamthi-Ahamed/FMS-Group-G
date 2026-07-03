import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class StudentDashboard extends JFrame implements ActionListener{

    private static final int FRAME_WIDTH=940;
    private static final int FRAME_HEIGHT =565;

    private JButton profileNavButton;
    private JButton timetableNavButton;
    private JButton courseNavButton;
    private JButton logoutButton;
    private JTextField fullNameField;
    private JTextField studentIdField;
    private JTextField degreeField;
    private JTextField emailField;
    private JTextField mobileField;
    private JButton    saveButton;
    private final String                     loggedInUsername;
    private       DBConnector.StudentRecord  studentRecord;

    private final Color THEME_PURPLE= new Color(98, 60, 234);
    private final Color LIGHT_PURPLE_TEXT=new Color(225, 220, 250);

    public StudentDashboard(String username){
        this.loggedInUsername=username;
        setTitle("Faculty Management System - Student Dashboard");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        studentRecord =DBConnector.getStudentByUsername(username);
        getContentPane().add(buildSidebarPanel(), BorderLayout.WEST);
        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);}

    private JPanel buildSidebarPanel(){
        JPanel p =new JPanel(null);
        p.setPreferredSize(new Dimension(370, FRAME_HEIGHT));
        p.setBackground(THEME_PURPLE);

        JLabel avatarLabel= new JLabel("\uD83D\uDC64", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 60));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setBounds(0, 30, 370, 80);
        p.add(avatarLabel);

        String firstName=loggedInUsername.contains(" ")
                ? loggedInUsername.split(" ")[0] : loggedInUsername;
        JLabel welcomeLabel=new JLabel("Welcome, " + firstName, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(15, 115, 340, 35);
        p.add(welcomeLabel);

        ImageIcon profileIcon=new ImageIcon("resource/img/contact.png");
        profileNavButton=new JButton("  Profile Details",profileIcon);
        styleNavButton(profileNavButton, true);
        profileNavButton.setBounds(50, 175, 270, 45);
        profileNavButton.addActionListener(this);
        p.add(profileNavButton);

        ImageIcon timetableIcon=new ImageIcon("resource/img/calendar-clock.png");
        timetableNavButton=new JButton("  Time table",timetableIcon);
        styleNavButton(timetableNavButton, false);
        timetableNavButton.setBounds(50, 230, 270, 45);
        timetableNavButton.addActionListener(this);
        p.add(timetableNavButton);

        ImageIcon courseIcon=new ImageIcon("resource/img/open-book.png");
        courseNavButton=new JButton("  Course Enrolled",courseIcon);
        styleNavButton(courseNavButton, false);
        courseNavButton.setBounds(50, 285, 270, 45);
        courseNavButton.addActionListener(this);
        p.add(courseNavButton);
        ImageIcon logoutIcon=new ImageIcon("resource/img/back-arrow.png");
        logoutButton=new JButton(logoutIcon);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 22));
        logoutButton.setForeground(THEME_PURPLE);
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBounds(160, 400, 50, 50);
        logoutButton.addActionListener(this);
        p.add(logoutButton);
        return p;}
    private void styleNavButton(JButton btn, boolean selected){
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        if(selected){
            btn.setBackground(Color.WHITE);
            btn.setForeground(THEME_PURPLE);
        }else{
            btn.setBackground(THEME_PURPLE);
            btn.setForeground(LIGHT_PURPLE_TEXT);
            btn.setBorder(BorderFactory.createLineBorder(LIGHT_PURPLE_TEXT, 1, true));
        }}

    private JPanel buildContentPanel(){
        JPanel p=new JPanel(null);
        p.setPreferredSize(new Dimension(FRAME_WIDTH - 370, FRAME_HEIGHT));
        p.setBackground(Color.WHITE);
        JLabel heading =new JLabel("Profile Details");
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setForeground(THEME_PURPLE);
        heading.setBounds(195, 40, 300, 35);
        p.add(heading);

        addLabel(p, "Full Name",     20, 110);
        fullNameField =addField(p, 220, 107, 280,
                studentRecord != null ? studentRecord.fullName : "");
        fullNameField.setEditable(false);
        fullNameField.setBackground(new Color(245,245,245));

        addLabel(p, "Student ID",    20, 165);
        studentIdField=addField(p, 220, 162, 280,
                studentRecord != null ? studentRecord.studentId : "");
        studentIdField.setEditable(false);
        studentIdField.setBackground(new Color(245,245,245));

        /// Degree
        addLabel(p, "Degree",        20, 220);
        degreeField =addField(p, 220, 217, 280,
                studentRecord != null ? studentRecord.degree : "");
        degreeField.setEditable(false);
        degreeField.setBackground(new Color(245,245,245));

        ///Email
        addLabel(p, "Email",         20, 275);
        emailField =addField(p, 220, 272, 280,
                studentRecord != null ? studentRecord.email : "");

        /// Mobile
        addLabel(p, "Mobile Number", 20, 330);
        mobileField = addField(p, 220, 327, 280,
                studentRecord != null ? studentRecord.mobile : "");

        saveButton =new JButton("Save changes");
        saveButton.setFont(new Font("Arial", Font.BOLD, 17));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(THEME_PURPLE);
        saveButton.setFocusPainted(false);
        saveButton.setBounds(110, 405, 370, 45);
        saveButton.addActionListener(this);
        p.add(saveButton);
        return p;
    }

    private void addLabel(JPanel panel, String text, int x, int y){
        JLabel lbl =new JLabel(text);
        lbl.setFont(new Font("Arial", Font.BOLD, 15));
        lbl.setForeground(THEME_PURPLE);
        lbl.setBounds(x, y, 190, 25);
        panel.add(lbl);
    }

    private JTextField addField(JPanel panel, int x, int y, int w, String value){
        JTextField f =new JTextField(value);
        f.setFont(new Font("Arial", Font.PLAIN, 15));
        f.setBounds(x, y, w, 35);
        f.setBorder(BorderFactory.createLineBorder(THEME_PURPLE, 2, true));
        panel.add(f);
        return f;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        Object src =e.getSource();

        if(src == profileNavButton){
            styleNavButton(profileNavButton,   true);
            styleNavButton(timetableNavButton, false);
            styleNavButton(courseNavButton,    false);
        }else if(src == timetableNavButton){
            dispose();
            new TimetablePage(loggedInUsername).setVisible(true);
        } else if(src == courseNavButton) {
            dispose();
            new CoursesEnrolledPage(loggedInUsername).setVisible(true);
        } else if(src == logoutButton) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        } else if (src == saveButton) {
            saveChanges();
        }
    }
    private void saveChanges(){
        String email  = emailField.getText().trim();
        String mobile = mobileField.getText().trim();

        if(email.isEmpty() || mobile.isEmpty()){
            JOptionPane.showMessageDialog(this, "Email and Mobile Number cannot be empty.");
            return;
        }

        if(studentRecord == null){
            JOptionPane.showMessageDialog(this,
                    "Student record not found in the database.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean ok= DBConnector.updateStudent(studentRecord.pk, email, mobile);
        if(ok){
            JOptionPane.showMessageDialog(this, "Profile changes saved successfully.");
        }else{
            JOptionPane.showMessageDialog(this,
                    "Failed to save changes. Please try again.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }}