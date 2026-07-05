import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminLecturersPage extends JFrame implements ActionListener{
    private static final int FRAME_WIDTH=940;
    private static final int FRAME_HEIGHT=600;
    private static final int SIDEBAR_WIDTH=370;
    private final String adminUsername;
    private final Color THEME_PURPLE=new Color(98, 60, 234);
    private final Color LIGHT_PURPLE_TEXT=new Color(225, 220, 250);
    private final Color GRAY_BUTTON=new Color(190, 190, 190);

    private JButton studentsNavButton;
    private JButton lecturersNavButton;
    private JButton coursesNavButton;
    private JButton departmentsNavButton;
    private JButton degreesNavButton;
    private JButton logoutButton;
    private JButton addNewButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton saveChangesButton;
    private JTable adminTable;
    private DefaultTableModel tableModel;
    private static final String CURRENT_PAGE = "Lecturers";
    private final java.util.Set<Integer> editableRows = new java.util.HashSet<>();
    private final java.util.List<Integer> deletedIds = new java.util.ArrayList<>();

    public AdminLecturersPage() {
        this("admin");
    }
    public AdminLecturersPage(String adminUsername){
        this.adminUsername=adminUsername==null ? "admin" : adminUsername;
        setTitle("Faculty Management System - Admin Lecturers");
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildSidebarPanel(), BorderLayout.WEST);
        getContentPane().add(buildContentPanel(), BorderLayout.CENTER);
    }
    private JPanel buildSidebarPanel(){
        JPanel sidebarPanel=new JPanel(null);
        sidebarPanel.setPreferredSize(new Dimension(SIDEBAR_WIDTH, FRAME_HEIGHT));
        sidebarPanel.setBackground(THEME_PURPLE);

        JLabel welcomeLabel=new JLabel("Welcome, Admin");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 26));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(35, 60, 320, 35);
        sidebarPanel.add(welcomeLabel);
        ImageIcon profileIcon=new ImageIcon("resource/img/contact.png");
        studentsNavButton =new JButton(" Students",profileIcon);
        styleNavButton(studentsNavButton, false);
        studentsNavButton.setBounds(35, 135, 290, 45);
        studentsNavButton.addActionListener(this);
        sidebarPanel.add(studentsNavButton);
        ImageIcon lecturerIcon = new ImageIcon("resource/img/contact.png");
        lecturersNavButton = new JButton(" Lecturers",lecturerIcon);
        styleNavButton(lecturersNavButton, true);
        lecturersNavButton.setBounds(35, 190, 290, 45);
        lecturersNavButton.addActionListener(this);
        sidebarPanel.add(lecturersNavButton);

        ImageIcon courseIcon=new ImageIcon("resource/img/open-book.png");
        coursesNavButton =new JButton("  Courses",courseIcon);
        styleNavButton(coursesNavButton, false);
        coursesNavButton.setBounds(35, 245, 290, 45);
        coursesNavButton.addActionListener(this);
        sidebarPanel.add(coursesNavButton);

        ImageIcon departmentIcon=new ImageIcon("resource/img/bank.png");
        departmentsNavButton=new JButton("  Departments",departmentIcon);
        styleNavButton(departmentsNavButton, false);
        departmentsNavButton.setBounds(35, 300, 290, 45);
        departmentsNavButton.addActionListener(this);
        sidebarPanel.add(departmentsNavButton);
        ImageIcon degreeIcon=new ImageIcon("resource/img/mortarboard.png");
        degreesNavButton=new JButton("  Degrees",degreeIcon);
        styleNavButton(degreesNavButton, false);
        degreesNavButton.setBounds(35, 355, 290, 45);
        degreesNavButton.addActionListener(this);
        sidebarPanel.add(degreesNavButton);

        ImageIcon logoutIcon = new ImageIcon("resource/img/back-arrow.png");
        logoutButton = new JButton(logoutIcon);
        logoutButton.setFont(new Font("Arial", Font.BOLD, 22));
        logoutButton.setForeground(THEME_PURPLE);
        logoutButton.setBackground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBounds(160, 460, 50, 50);
        logoutButton.addActionListener(this);
        sidebarPanel.add(logoutButton);
        return sidebarPanel;
    }

    private void styleNavButton(JButton button, boolean selected){
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        if(selected){
            button.setBackground(Color.WHITE);
            button.setForeground(THEME_PURPLE);
            button.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
        }else{
            button.setBackground(THEME_PURPLE);
            button.setForeground(LIGHT_PURPLE_TEXT);
            button.setBorder(BorderFactory.createLineBorder(LIGHT_PURPLE_TEXT, 1, true));
        }
    }
    private JPanel buildContentPanel(){
        JPanel contentPanel=new JPanel(null);
        contentPanel.setPreferredSize(new Dimension(FRAME_WIDTH - SIDEBAR_WIDTH, FRAME_HEIGHT));
        contentPanel.setBackground(Color.WHITE);

        JLabel headingLabel = new JLabel("Lecturers", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 26));
        headingLabel.setForeground(THEME_PURPLE);
        headingLabel.setBounds(0, 30, FRAME_WIDTH - SIDEBAR_WIDTH, 35);
        contentPanel.add(headingLabel);

        addNewButton=buildTopButton("Add new", THEME_PURPLE);
        addNewButton.setBounds(30, 90, 140, 40);
        contentPanel.add(addNewButton);
        editButton=buildTopButton("Edit", GRAY_BUTTON);
        editButton.setBounds(210, 90, 140, 40);
        contentPanel.add(editButton);
        deleteButton=buildTopButton("Delete", GRAY_BUTTON);
        deleteButton.setBounds(390, 90, 140, 40);
        contentPanel.add(deleteButton);

        String[] columnNames={"DB ID","Full Name","Department","Courses teaching","Email", "Mobile Number"};
        tableModel=new DefaultTableModel(DBConnector.getAdminLecturerRows(), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && editableRows.contains(row);
            }
        };

        adminTable = new JTable(tableModel);
        adminTable.setFont(new Font("Arial", Font.BOLD, 13));
        adminTable.setForeground(THEME_PURPLE);
        adminTable.setRowHeight(45);
        adminTable.setSelectionBackground(LIGHT_PURPLE_TEXT);
        adminTable.setSelectionForeground(THEME_PURPLE);
        adminTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        adminTable.getTableHeader().setForeground(THEME_PURPLE);
        adminTable.getTableHeader().setResizingAllowed(false);
        adminTable.getTableHeader().setReorderingAllowed(false);
        adminTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        hideIdColumn(adminTable);
        centerTableContent(adminTable);
        fitColumnWidths(adminTable);

        int headerHeight=adminTable.getTableHeader().getPreferredSize().height;
        int visibleDataRows=4;
        int tableVisibleHeight=headerHeight+(adminTable.getRowHeight()*visibleDataRows);
        JScrollPane tableScrollPane=new JScrollPane(adminTable);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        tableScrollPane.setViewportBorder(null);
        tableScrollPane.getViewport().setBackground(Color.WHITE);

        int scrollBarWidth= tableScrollPane.getVerticalScrollBar().getPreferredSize().width;
        int scrollPaneWidth =Math.min(adminTable.getPreferredSize().width+scrollBarWidth+ 4,
                (FRAME_WIDTH-SIDEBAR_WIDTH)-60);
        tableScrollPane.setBounds(30, 175, scrollPaneWidth, tableVisibleHeight + 4);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(THEME_PURPLE, 2));
        contentPanel.add(tableScrollPane);

        saveChangesButton =new JButton("Save changes");
        saveChangesButton.setFont(new Font("Arial", Font.BOLD, 17));
        saveChangesButton.setForeground(Color.WHITE);
        saveChangesButton.setBackground(THEME_PURPLE);
        saveChangesButton.setFocusPainted(false);
        saveChangesButton.setBounds(30, 420, scrollPaneWidth, 45);
        saveChangesButton.addActionListener(this);
        contentPanel.add(saveChangesButton);
        return contentPanel;
    }

    private JButton buildTopButton(String text, Color background){
        JButton button =new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.addActionListener(this);
        return button;
    }
    private void hideIdColumn(JTable table){
        TableColumn idColumn = table.getColumnModel().getColumn(0);
        idColumn.setMinWidth(0);
        idColumn.setMaxWidth(0);
        idColumn.setPreferredWidth(0);
        idColumn.setResizable(false);
    }
    private void centerTableContent(JTable table){
        DefaultTableCellRenderer centerRenderer =new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i=0; i<table.getColumnCount();i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
    private void fitColumnWidths(JTable table){
        FontMetrics metrics=table.getFontMetrics(table.getFont());
        int total=0;
        for (int col=1;col<table.getColumnCount();col++) {
            int width=metrics.stringWidth(table.getColumnName(col));
            for (int row=0; row<table.getRowCount();row++) {
                Object value = table.getValueAt(row, col);
                int cellWidth =metrics.stringWidth(value==null ? "" : value.toString());
                width = Math.max(width, cellWidth);
            }
            width+= 30;
            table.getColumnModel().getColumn(col).setPreferredWidth(width);
            table.getColumnModel().getColumn(col).setMinWidth(Math.min(width, 70));
            table.getColumnModel().getColumn(col).setMaxWidth(width);
            total +=width;
        }
        int scrollBarWidth= new JScrollPane(table).getVerticalScrollBar().getPreferredSize().width;
        int maxTableWidth=(FRAME_WIDTH - SIDEBAR_WIDTH) - 60 - scrollBarWidth - 4;
        if (total > maxTableWidth) {
            double scale=(double) maxTableWidth / (double) total;
            for (int col=1; col<table.getColumnCount();col++){
                int currentWidth= table.getColumnModel().getColumn(col).getPreferredWidth();
                int scaledWidth =Math.max(60,(int)(currentWidth *scale));
                table.getColumnModel().getColumn(col).setMinWidth(scaledWidth);
                table.getColumnModel().getColumn(col).setMaxWidth(scaledWidth);
                table.getColumnModel().getColumn(col).setPreferredWidth(scaledWidth);
            }
        }
    }
    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if(source==studentsNavButton) {
            openStudentsPage();
        }else if(source==lecturersNavButton) {
            openLecturersPage();
        }else if(source==coursesNavButton) {
            openCoursesPage();
        }else if(source==departmentsNavButton) {
            openDepartmentsPage();
        }else if(source==degreesNavButton) {
            openDegreesPage();
        }else if(source==logoutButton) {
            handleLogout();
        }else if(source== addNewButton) {
            addNewRow();
        }else if(source==editButton) {
            editSelectedRow();
        }else if(source ==deleteButton) {
            deleteSelectedRow();
        }else if(source== saveChangesButton) {
            saveChanges();
        }
    }

    private void openStudentsPage(){
        if(CURRENT_PAGE.equals("Students"))return;
        dispose();
        new AdminStudentsPage(adminUsername).setVisible(true);
    }
    private void openLecturersPage(){
        if(CURRENT_PAGE.equals("Lecturers")) return;
        dispose();
        new AdminLecturersPage(adminUsername).setVisible(true);
    }

    private void openCoursesPage(){
        if(CURRENT_PAGE.equals("Courses")) return;
        dispose();
        new AdminCoursesPage(adminUsername).setVisible(true);
    }

    private void openDepartmentsPage(){
        if (CURRENT_PAGE.equals("Departments")) return;
        dispose();
        new AdminDepartmentsPage(adminUsername).setVisible(true);
    }

    private void openDegreesPage(){
        if (CURRENT_PAGE.equals("Degrees")) return;
        dispose();
        new AdminDegreesPage(adminUsername).setVisible(true);
    }

    private void handleLogout(){
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (choice==JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    private void addNewRow() {
        Object[] rowData=showRowDialog("Add New " + CURRENT_PAGE.substring(0, CURRENT_PAGE.length() - 1), null);
        if (rowData==null) {
            return;
        }
        tableModel.addRow(rowData);
        int newRowIndex=tableModel.getRowCount() - 1;
        editableRows.add(newRowIndex);
        adminTable.setRowSelectionInterval(newRowIndex,newRowIndex);
        fitColumnWidths(adminTable);
    }
    private void editSelectedRow() {
        int selectedRow=adminTable.getSelectedRow();
        if (selectedRow ==-1) {
            JOptionPane.showMessageDialog(this, "Please select a row to edit.");
            return;
        }
        Object[] rowData=showRowDialog("Edit "+CURRENT_PAGE.substring(0,CURRENT_PAGE.length()-1),selectedRow);
        if (rowData==null) {
            return;
        }

        for (int col=0;col<tableModel.getColumnCount();col++){
            tableModel.setValueAt(rowData[col],selectedRow,col);
        }
        editableRows.add(selectedRow);
        tableModel.fireTableRowsUpdated(selectedRow, selectedRow);
        fitColumnWidths(adminTable);
    }
    private void deleteSelectedRow(){
        int selectedRow=adminTable.getSelectedRow();
        if(selectedRow==-1) {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.");
            return;
        }
        StringBuilder details=new StringBuilder();
        for (int col=1; col<tableModel.getColumnCount();col++) {
            details.append(tableModel.getColumnName(col))
                    .append(": ")
                    .append(valueAt(selectedRow, col))
                    .append("\n");
        }

        int choice =JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this record?\n\n" + details,
                "Delete "+CURRENT_PAGE.substring(0,CURRENT_PAGE.length() -1),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice!=JOptionPane.YES_OPTION){
            return;
        }
        int id=toInt(tableModel.getValueAt(selectedRow, 0));
        if (id >0) {
            deletedIds.add(id);
        }
        tableModel.removeRow(selectedRow);
        shiftEditableRowsAfterDelete(selectedRow);
    }

    private Object[] showRowDialog(String title, Integer rowIndex) {
        JPanel panel =new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5, 5, 5, 5);
        gbc.fill =GridBagConstraints.HORIZONTAL;
        int editableColumnCount = tableModel.getColumnCount() - 1;
        JTextField[] fields = new JTextField[editableColumnCount];
        for(int i=0;i<editableColumnCount;i++){
            int col=i+1;
            JLabel label=new JLabel(tableModel.getColumnName(col) + ":");
            label.setFont(new Font("Arial", Font.BOLD, 13));
            gbc.gridx=0;
            gbc.gridy=i;
            gbc.weightx=0;
            panel.add(label,gbc);
            fields[i]=new JTextField(22);
            fields[i].setText(rowIndex==null ? "" : valueAt(rowIndex, col));
            gbc.gridx=1;
            gbc.gridy=i;
            gbc.weightx=1;
            panel.add(fields[i],gbc);
        }
        int result =JOptionPane.showConfirmDialog(this,panel,title,
                JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE);
        if (result !=JOptionPane.OK_OPTION){
            return null;
        }
        Object[] rowData =new Object[tableModel.getColumnCount()];
        rowData[0]=rowIndex==null ? "" : tableModel.getValueAt(rowIndex, 0);
        for (int i=0;i<editableColumnCount;i++){
            rowData[i+1]=fields[i].getText().trim();
        }
        return rowData;
    }
    private void shiftEditableRowsAfterDelete(int deletedRow) {
        java.util.Set<Integer> updated=new java.util.HashSet<>();
        for(int rowIndex : editableRows){
            if(rowIndex<deletedRow) {
                updated.add(rowIndex);
            }else if(rowIndex>deletedRow) {
                updated.add(rowIndex-1);
            }
        }
        editableRows.clear();
        editableRows.addAll(updated);
    }

    private void saveChanges(){
        if (adminTable.isEditing()){
            adminTable.getCellEditor().stopCellEditing();
        }
        boolean success;
        try{
            success =saveTableRowsToDatabase();
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Number", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (success){
            editableRows.clear();
            deletedIds.clear();
            reloadRowsFromDatabase();
            JOptionPane.showMessageDialog(this, "Lecturer changes saved successfully.");
        }else{
            JOptionPane.showMessageDialog(this,
                    "Save failed.\n" + DBConnector.getLastError(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void reloadRowsFromDatabase(){
        tableModel.setRowCount(0);
        Object[][] rows=DBConnector.getAdminLecturerRows();
        for (Object[] row : rows) {
            tableModel.addRow(row);
        }
        fitColumnWidths(adminTable);
    }
    private boolean saveTableRowsToDatabase(){
        java.util.List<DBConnector.AdminLecturerRecord>records=new java.util.ArrayList<>();
        for (int row=0;row<tableModel.getRowCount();row++){
            records.add(new DBConnector.AdminLecturerRecord(
                    toInt(tableModel.getValueAt(row, 0)),
                    valueAt(row, 1),
                    valueAt(row, 2),
                    valueAt(row, 3),
                    valueAt(row, 4),
                    valueAt(row, 5)
            ));
        }
        return DBConnector.saveAdminLecturers(records, deletedIds);
    }
    private String valueAt(int row, int column){
        Object value=tableModel.getValueAt(row,column);
        return value==null ? "" : value.toString().trim();
    }
    private int toInt(Object value) {
        if(value==null || value.toString().trim().isEmpty()){
            return 0;
        }
        try{
            return Integer.parseInt(value.toString().trim());
        }catch(NumberFormatException ex) {
            return 0;
        }
    }
    private int toRequiredInt(int row,int column,String fieldName){
        String value =valueAt(row, column);
        if(value.isEmpty()){
            return 0;
        }
        try{
            return Integer.parseInt(value);
        }catch(NumberFormatException ex) {
            throw new NumberFormatException(fieldName+" must be a number on row "+(row+1)+".");
        }
    }
}
