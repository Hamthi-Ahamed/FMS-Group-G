import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class CoursesEnrolledPage extends JFrame implements ActionListener {

    private static final int FRAME_WIDTH  = 940;
    private static final int FRAME_HEIGHT = 565;

    private JButton profileNavButton;
    private JButton timetableNavButton;
    private JButton courseNavButton;
    private JButton logoutButton;

    private final String loggedInUsername;
    private final Color THEME_PURPLE= new Color(98, 60, 234);
    private final Color LIGHT_PURPLE_TEXT=new Color(225, 220, 250);

    public CoursesEnrolledPage(String username) {
        this.loggedInUsername = username;

        setTitle("Faculty Management System - Courses Enrolled");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildSidebarPanel(), BorderLayout.WEST);
        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);
    }

    private JPanel buildSidebarPanel() {
        JPanel p=new JPanel(null);
        p.setPreferredSize(new Dimension(370, FRAME_HEIGHT));
        p.setBackground(THEME_PURPLE);

        JLabel avatarLabel=new JLabel("\uD83D\uDC64", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 60));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setBounds(0, 30, 370, 80);
        p.add(avatarLabel);
        String firstName = loggedInUsername.contains(" ")
                ? loggedInUsername.split(" ")[0] : loggedInUsername;
        JLabel welcome=new JLabel("Welcome, " + firstName, SwingConstants.CENTER);
        welcome.setFont(new Font("Arial", Font.BOLD, 24));
        welcome.setForeground(Color.WHITE);
        welcome.setBounds(15, 115, 340, 35);
        p.add(welcome);

        ImageIcon profileIcon=new ImageIcon("resource/img/contact.png");
        profileNavButton=new JButton("  Profile Details",profileIcon);
        styleNavButton(profileNavButton, false);
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
        courseNavButton=new JButton("  Courses Enrolled",courseIcon);
        styleNavButton(courseNavButton, true);
        courseNavButton.setBounds(50, 285, 270, 45);
        courseNavButton.addActionListener(this);
        p.add(courseNavButton);

        ImageIcon logoutIcon=new ImageIcon("resource/img/back-arrow.png");
        logoutButton = new JButton(logoutIcon);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 22));
        logoutButton.setForeground(THEME_PURPLE);
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBounds(160, 400, 50, 50);
        logoutButton.addActionListener(this);
        p.add(logoutButton);

        return p;
    }

    private void styleNavButton(JButton btn, boolean selected) {
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
        }
    }

    private JPanel buildContentPanel() {
        JPanel p=new JPanel(null);
        p.setPreferredSize(new Dimension(FRAME_WIDTH-370,FRAME_HEIGHT));
        p.setBackground(Color.WHITE);

        JLabel heading = new JLabel("Courses Enrolled");
        heading.setFont(new Font("Arial",Font.BOLD,26));
        heading.setForeground(THEME_PURPLE);
        heading.setBounds(150, 40, 350, 35);
        p.add(heading);

        String[] columnNames = {"Course code","Course name","Credits","Grade"};

        String[][] courseRows = DBConnector.getEnrolledCourses(loggedInUsername);

        if (courseRows==null || courseRows.length==0) {
            courseRows=new String[][]{
                    {"ETEC 21062","OOP","2","A+"},
                    {"ETEC 21052","OOP","2","B"},
                    {"ETEC 21042","OOP","2","A"},
                    {"ETEC 21032","OOP","2","D"},
                    {"ETEC 21022","OOP","2","C"},
                    {"ETEC 21012","OOP","2","B"}
            };
        }

        JTable table=new JTable(courseRows, columnNames);
        table.setFont(new Font("Arial", Font.BOLD, 14));
        table.setForeground(THEME_PURPLE);
        table.setRowHeight(45);
        table.setEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setForeground(THEME_PURPLE);
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for(int i=0;i<table.getColumnCount();i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }

        FontMetrics fm = table.getFontMetrics(table.getFont());
        for(int col=0;col<columnNames.length;col++){
            int w=fm.stringWidth(columnNames[col]);
            for (String[] row : courseRows) {
                int cw=fm.stringWidth(row[col]);
                if (cw>w) w=cw;
            }
            w += 30;
            table.getColumnModel().getColumn(col).setPreferredWidth(w);
            table.getColumnModel().getColumn(col).setMinWidth(w);
            table.getColumnModel().getColumn(col).setMaxWidth(w);
        }

        int tableW= table.getPreferredSize().width;
        int headerH=table.getTableHeader().getPreferredSize().height;
        int visibleRows= 6;
        int visibleH= headerH+(table.getRowHeight()*visibleRows);

        JScrollPane sp=new JScrollPane(table);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setViewportBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        int scrollW=sp.getVerticalScrollBar().getPreferredSize().width;
        sp.setBounds(50, 100,tableW + scrollW + 4, visibleH + 4);
        sp.setBorder(BorderFactory.createLineBorder(THEME_PURPLE, 2));
        p.add(sp);
        return p;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        if(src == profileNavButton){
            dispose();
            new StudentDashboard(loggedInUsername).setVisible(true);

        }else if(src == timetableNavButton){
            dispose();
            new TimetablePage(loggedInUsername).setVisible(true);

        }else if(src == courseNavButton){

        }else if(src == logoutButton){
            int choice=JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?", "Logout",
                    JOptionPane.YES_NO_OPTION);
            if(choice==JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        }
    }
}