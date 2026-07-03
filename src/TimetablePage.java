import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class TimetablePage extends JFrame implements ActionListener {

    private static final int FRAME_WIDTH  = 940;
    private static final int FRAME_HEIGHT = 565;

    private JButton profileNavButton;
    private JButton timetableNavButton;
    private JButton courseNavButton;
    private JButton logoutButton;

    private final String loggedInUsername;

    private final Color THEME_PURPLE=new Color(98, 60, 234);
    private final Color LIGHT_PURPLE_TEXT=new Color(225, 220, 250);

    public TimetablePage(String username){
        this.loggedInUsername=username;
        setTitle("Faculty Management System - Time table");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildSidebarPanel(), BorderLayout.WEST);
        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);}

    private JPanel buildSidebarPanel(){
        JPanel p=new JPanel(null);
        p.setPreferredSize(new Dimension(370, FRAME_HEIGHT));
        p.setBackground(THEME_PURPLE);
        JLabel avatarLabel = new JLabel("\uD83D\uDC64", SwingConstants.CENTER);
        avatarLabel.setFont(new Font("SansSerif", Font.PLAIN, 60));
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setBounds(0, 30, 370, 80);
        p.add(avatarLabel);
        String firstName=loggedInUsername.contains(" ")
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
        styleNavButton(timetableNavButton, true);
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
        return p;
    }

    private void styleNavButton(JButton btn, boolean selected){
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        if(selected){
            btn.setBackground(Color.WHITE);
            btn.setForeground(THEME_PURPLE);
        }else {
            btn.setBackground(THEME_PURPLE);
            btn.setForeground(LIGHT_PURPLE_TEXT);
            btn.setBorder(BorderFactory.createLineBorder(LIGHT_PURPLE_TEXT, 1, true));
        }
    }
    private JPanel buildContentPanel(){
        JPanel p=new JPanel(null);
        p.setPreferredSize(new Dimension(FRAME_WIDTH - 370, FRAME_HEIGHT));
        p.setBackground(Color.WHITE);
        JLabel heading=new JLabel("Time table");
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        heading.setForeground(THEME_PURPLE);
        heading.setBounds(200, 40, 300, 35);
        p.add(heading);
        String[][] allRows = DBConnector.getTimetableForStudent(loggedInUsername);
        java.util.List<String[]> morningList   = new java.util.ArrayList<>();
        java.util.List<String[]> afternoonList = new java.util.ArrayList<>();

        for (String[] row : allRows) {
            String slot = row[0];
            int hour = 0;
            try { hour = Integer.parseInt(slot.split(":")[0]); } catch (Exception ignored) {}
            if (hour < 12) {
                morningList.add(row);
            } else {
                afternoonList.add(row);
            }
        }

        String[][] morningRows   = morningList.toArray(new String[0][]);
        String[][] afternoonRows = afternoonList.toArray(new String[0][]);
        if(morningRows.length == 0) {
            morningRows = new String[][]{
                    {"08:00","OOP","OOP","OOP","OOP","OOP"},
                    {"10:00","OOP","OOP","OOP","OOP","OOP"}
            };
        }
        if(afternoonRows.length==0) {
            afternoonRows=new String[][]{
                    {"13:00","SE","OOP","SE","SE","SE"},
                    {"15:00","SE","OOP","SE","SE","SE"}
            };
        }

        String[] columnNames={"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        JTable morningTable=buildTable(morningRows, columnNames, true);
        Dimension mSize=morningTable.getPreferredSize();
        int tableW=mSize.width;
        int headerH=morningTable.getTableHeader().getPreferredSize().height;
        int morningTotalH=headerH + mSize.height;
        JScrollPane mScroll = wrapInScrollPane(morningTable, tableW, morningTotalH);
        mScroll.setBounds(50, 100, tableW + 4, morningTotalH + 4);
        p.add(mScroll);
        int intervalY = 100 + morningTotalH + 4;
        JLabel interval=new JLabel("Interval", SwingConstants.CENTER);
        interval.setFont(new Font("Arial", Font.BOLD, 18));
        interval.setForeground(Color.WHITE);
        interval.setOpaque(true);
        interval.setBackground(THEME_PURPLE);
        interval.setBounds(50, intervalY, tableW + 4, 40);
        p.add(interval);
        JTable afternoonTable=buildTable(afternoonRows, columnNames, false);
        Dimension aSize=afternoonTable.getPreferredSize();
        JScrollPane aScroll = wrapInScrollPane(afternoonTable, tableW, aSize.height);
        aScroll.setBounds(50, intervalY + 40, tableW + 4, aSize.height + 4);
        p.add(aScroll);
        return p;
    }
    private JTable buildTable(String[][] rows, String[] cols, boolean showHeader){
        JTable table = new JTable(rows, cols);
        table.setFont(new Font("Arial", Font.BOLD, 14));
        table.setForeground(THEME_PURPLE);
        table.setRowHeight(45);
        table.setEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.setCellSelectionEnabled(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        if(showHeader){
            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
            table.getTableHeader().setForeground(THEME_PURPLE);
            table.getTableHeader().setResizingAllowed(false);
            table.getTableHeader().setReorderingAllowed(false);
        }else{
            table.setTableHeader(null);}
        DefaultTableCellRenderer center=new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);}
        FontMetrics fm = table.getFontMetrics(table.getFont());
        for (int col = 0; col < cols.length; col++){
            int w = fm.stringWidth(cols[col]);
            for (String[] row : rows){
                int cw = fm.stringWidth(row[col]);
                if(cw > w) w = cw;
            }
            w += 16;
            table.getColumnModel().getColumn(col).setPreferredWidth(w);
            table.getColumnModel().getColumn(col).setMinWidth(w);
            table.getColumnModel().getColumn(col).setMaxWidth(w);
        }
        return table;
    }

    private JScrollPane wrapInScrollPane(JTable table, int w, int h) {
        JScrollPane sp=new JScrollPane(table);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setViewportBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(THEME_PURPLE, 2));
        return sp;
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == profileNavButton) {
            dispose();
            new StudentDashboard(loggedInUsername).setVisible(true);
        }else if (src==timetableNavButton) {
        }else if (src==courseNavButton) {
            dispose();
            new CoursesEnrolledPage(loggedInUsername).setVisible(true);
        }else if (src==logoutButton) {
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