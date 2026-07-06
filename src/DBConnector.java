import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DBConnector {
    private static final String URL =
            "jdbc:mysql://localhost:3306/faculty%20mangement%20system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
     private static final String DB_PASS = "";

    private static String lastError = "";


    public static String getLastError() {
        return lastError ==null || lastError.trim().isEmpty() ? "Unknown database error." : lastError;
    }

    private static void setLastError(Exception e) {
        lastError =e.getMessage();
         e.printStackTrace();
    }

    private static void setLastError(String message) {
        lastError = message;
        System.err.println(message);
    }


    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add the mysql-connector-j JAR to your classpath.", e);
        }
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }



    public static String verifyLogin(String username, String password) {
        String sql ="SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection con = getConnection();
             PreparedStatement ps =con.prepareStatement(sql)) {

            ps.setString(1, username);
             ps.setString(2, password); // plain-text for now; swap for hashed later

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            setLastError(e);
        }
        return null;
    }


    public static boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection con =getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
             ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            setLastError("Username already exists: " + username);
            return false;
        } catch (SQLException e) {
            setLastError(e);
             return false;
        }
    }




    public static StudentRecord getStudentByUsername(String username) {
        String sql = "SELECT s.full_name, s.student_id, d.name AS degree, " +
                "s.email, s.mobile_number, s.id AS student_pk " +
                "FROM students s " +
                "JOIN users u ON s.user_id =u.id " +
                "JOIN degrees d ON s.degree_id = d.id " +
                "WHERE u.username = ?";

        try (Connection con = getConnection();
             PreparedStatement ps =con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new StudentRecord(
                        rs.getInt("student_pk"),
                        rs.getString("full_name"),
                         rs.getString("student_id"),
                        rs.getString("degree"),
                        rs.getString("email"),
                        rs.getString("mobile_number")
                );
            }
        } catch (SQLException e) {
            setLastError(e);
        }
        return null;
    }


    public static boolean updateStudent(int studentPk, String email, String mobile) {
        String sql="UPDATE students SET email = ?, mobile_number = ? WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, mobile);
             ps.setInt(3, studentPk);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            setLastError(e);
            return false;
        }
    }


    public static String[][] getTimetableForStudent(String username) {
        String sql ="SELECT t.day, t.time_slot, c.course_name " +
                "FROM timetable t " +
                "JOIN courses c ON t.course_id = c.id " +
                "JOIN enrollments e ON e.course_id = c.id " +
                "JOIN students s ON s.id = e.student_id " +
                "JOIN users u ON u.id = s.user_id " +
                "WHERE u.username = ? " +
                "ORDER BY t.time_slot, FIELD(t.day,'Monday','Tuesday','Wednesday','Thursday','Friday')";

        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        String[] days={"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        try (Connection con=getConnection();
             PreparedStatement ps =con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String slot=rs.getString("time_slot");
                String day=rs.getString("day");
                String name=rs.getString("course_name");
                map.putIfAbsent(slot, new String[]{"", "", "", "", ""});
                for (int i = 0; i < days.length; i++) {
                    if (days[i].equals(day)) {
                        map.get(slot)[i] = name;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            setLastError(e);
        }

        String[][] rows=new String[map.size()][6];
        int r = 0;
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            rows[r][0]=entry.getKey();
            System.arraycopy(entry.getValue(), 0, rows[r], 1, 5);
            r++;
        }
        return rows;
    }


    public static String[][] getEnrolledCourses(String username) {
        String sql = "SELECT c.course_code, c.course_name, c.credits, e.grade " +
                "FROM enrollments e " +
                "JOIN courses c ON c.id = e.course_id " +
                "JOIN students s ON s.id = e.student_id " +
                "JOIN users u ON u.id = s.user_id " +
                "WHERE u.username = ?";

        List<String[]> list = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs=ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        String.valueOf(rs.getInt("credits")),
                        rs.getString("grade") == null ? "-" : rs.getString("grade")
                });
            }
        } catch (SQLException e) {
            setLastError(e);
        }
        return list.toArray(new String[0][]);
    }

    public static class StudentRecord {
        public final int pk;
        public final String fullName;
        public final String studentId;
        public final String degree;
        public final String email;
        public final String mobile;

        public StudentRecord(int pk, String fullName, String studentId,
                             String degree, String email, String mobile) {
            this.pk = pk;
            this.fullName=fullName;
            this.studentId=studentId;
            this.degree=degree;
            this.email=email;
            this.mobile=mobile;
        }
    }



    public static Object[][] getAdminStudentRows() {
        String sql ="SELECT s.id, s.full_name, s.student_id, d.name AS degree, " +
                "s.email, s.mobile_number " +
                "FROM students s " +
                "JOIN degrees d ON d.id = s.degree_id " +
                "ORDER BY s.id";
        List<Object[]> rows=new ArrayList<>();
        try (Connection con=getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs=ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[]{
                          rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("student_id"),
                         rs.getString("degree"),
                        rs.getString("email"),
                        rs.getString("mobile_number")
                });
            }
        } catch (SQLException e) {
            setLastError(e);
        }
        return rows.toArray(new Object[0][]);
    }

    public static Object[][] getAdminLecturerRows() {
        String sql="SELECT l.id, l.full_name, d.name AS department, " +
                "COALESCE(GROUP_CONCAT(c.course_code ORDER BY c.id SEPARATOR ', '), '') AS courses_teaching, " +
                "l.email, l.mobile_number " +
                "FROM lecturers l " +
                "JOIN departments d ON d.id = l.department_id " +
                "LEFT JOIN courses c ON c.lecturer_id = l.id " +
                "GROUP BY l.id, l.full_name, d.name, l.email, l.mobile_number " +
                "ORDER BY l.id";
        List<Object[]> rows=new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs =ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("department"),
                        rs.getString("courses_teaching"),
                         rs.getString("email"),
                        rs.getString("mobile_number")
                });
            }
        } catch (SQLException e) {
            setLastError(e);
        }
        return rows.toArray(new Object[0][]);
    }

    public static Object[][] getAdminCourseRows() {
        String sql = "SELECT c.id, c.course_code, c.course_name, c.credits, l.full_name AS lecturer " +
                "FROM courses c " +
                "JOIN lecturers l ON l.id = c.lecturer_id " +
                "ORDER BY c.id";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps= con.prepareStatement(sql);
             ResultSet rs =ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("course_code"),
                        rs.getString("course_name"),
                        rs.getInt("credits"),
                         rs.getString("lecturer")
                });
            }
        } catch (SQLException e) {
            setLastError(e);
        }
        return rows.toArray(new Object[0][]);
    }

    public static Object[][] getAdminDepartmentRows() {
        String sql = "SELECT d.id, d.name, COALESCE(l.full_name, '') AS hod, " +
                "COALESCE(GROUP_CONCAT(deg.name ORDER BY deg.id SEPARATOR ', '), '') AS degree, " +
                "d.no_of_staff " +
                "FROM departments d " +
                "LEFT JOIN lecturers l ON l.id = d.hod_lecturer_id " +
                "LEFT JOIN degrees deg ON deg.department_id = d.id " +
                "GROUP BY d.id, d.name, l.full_name, d.no_of_staff " +
                "ORDER BY d.id";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con= getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs =ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("hod"),
                         rs.getString("degree"),
                        rs.getInt("no_of_staff")
                });
            }
        } catch (SQLException e) {
            setLastError(e);
        }
        return rows.toArray(new Object[0][]);
    }

    public static Object[][] getAdminDegreeRows() {
        String sql ="SELECT deg.id, deg.name AS degree, d.name AS department, deg.no_of_students " +
                "FROM degrees deg " +
                "JOIN departments d ON d.id = deg.department_id " +
                "ORDER BY deg.id";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps= con.prepareStatement(sql);
             ResultSet rs =ps.executeQuery()) {

            while (rs.next()) {
                rows.add(new Object[]{
                        rs.getInt("id"),
                        rs.getString("degree"),
                         rs.getString("department"),
                        rs.getInt("no_of_students")
                });
            }
        } catch (SQLException e) {
             setLastError(e);
        }
        return rows.toArray(new Object[0][]);
    }



    public static class AdminStudentRecord {
        public final int id;
        public final String fullName;
        public final String studentId;
        public final String degree;
        public final String email;
         public final String mobile;

        public AdminStudentRecord(int id, String fullName, String studentId, String degree, String email, String mobile) {
            this.id = id;
            this.fullName = safe(fullName);
            this.studentId = safe(studentId);
            this.degree = safe(degree);
             this.email = safe(email);
            this.mobile = safe(mobile);
        }

        public boolean isBlank() {
            return fullName.isEmpty() && studentId.isEmpty() && degree.isEmpty() && email.isEmpty() && mobile.isEmpty();
        }
    }

    public static class AdminLecturerRecord {
        public final int id;
        public final String fullName;
        public final String department;
        public final String coursesTeaching;
        public final String email;
         public final String mobile;

        public AdminLecturerRecord(int id, String fullName, String department, String coursesTeaching, String email, String mobile) {
            this.id = id;
            this.fullName= safe(fullName);
            this.department = safe(department);
            this.coursesTeaching = safe(coursesTeaching);
             this.email =safe(email);
            this.mobile= safe(mobile);
        }

        public boolean isBlank() {
            return fullName.isEmpty() && department.isEmpty() && coursesTeaching.isEmpty() && email.isEmpty() && mobile.isEmpty();
        }
    }

    public static class AdminCourseRecord {
        public final int id;
        public final String courseCode;
        public final String courseName;
         public final int credits;
        public final String lecturer;

        public AdminCourseRecord(int id, String courseCode, String courseName, int credits, String lecturer) {
            this.id=id;
            this.courseCode=safe(courseCode);
            this.courseName=safe(courseName);
            this.credits=credits;
            this.lecturer =safe(lecturer);}

        public boolean isBlank() {
            return courseCode.isEmpty() && courseName.isEmpty() && lecturer.isEmpty();
        }
    }

    public static class AdminDepartmentRecord{
        public final int id;
        public final String name;
        public final String hod;
        public final String degree;
        public final int noOfStaff;

        public AdminDepartmentRecord(int id, String name, String hod, String degree, int noOfStaff){
            this.id= id;
            this.name =safe(name);
            this.hod= safe(hod);
            this.degree=safe(degree);
            this.noOfStaff=noOfStaff;
        }
        public boolean isBlank() {
            return name.isEmpty() && hod.isEmpty() && degree.isEmpty();
        }}

    public static class AdminDegreeRecord {
        public final int id;
        public final String degree;
        public final String department;
        public final int noOfStudents;
        public AdminDegreeRecord(int id, String degree, String department, int noOfStudents) {
            this.id = id;
            this.degree = safe(degree);
            this.department = safe(department);
            this.noOfStudents = noOfStudents;
        }

        public boolean isBlank() {
            return degree.isEmpty() && department.isEmpty();
        }
    }


    public static boolean saveAdminStudents(List<AdminStudentRecord> rows, List<Integer> deletedIds) {
        try (Connection con=getConnection()) {
            con.setAutoCommit(false);
            try {
                for(Integer id : deletedIds) {
                    deleteStudent(con, id);
                }

                for(AdminStudentRecord row : rows) {
                    if(row.isBlank()) {
                        continue;
                    }
                    validateRequired(row.fullName, "Student full name");
                    validateRequired(row.studentId, "Student ID");
                    validateRequired(row.degree, "Degree");
                    validateRequired(row.email, "Student email");
                    int degreeId = requireDegreeId(con, row.degree);

                    if (row.id > 0) {
                        String update="UPDATE students SET full_name = ?, student_id = ?, degree_id = ?, email = ?, mobile_number = ? WHERE id = ?";
                        try (PreparedStatement ps = con.prepareStatement(update)) {
                            ps.setString(1, row.fullName);
                            ps.setString(2, row.studentId);
                            ps.setInt(3, degreeId);
                            ps.setString(4, row.email);
                            ps.setString(5, row.mobile);
                            ps.setInt(6, row.id);
                            ps.executeUpdate();
                        }
                    } else {
                        int userId=createUser(con, row.fullName, "Student");
                        String insert="INSERT INTO students (user_id, student_id, full_name, email, mobile_number, degree_id) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement ps=con.prepareStatement(insert)) {
                            ps.setInt(1, userId);
                            ps.setString(2, row.studentId);
                            ps.setString(3, row.fullName);
                            ps.setString(4, row.email);
                            ps.setString(5, row.mobile);
                            ps.setInt(6, degreeId);
                            ps.executeUpdate();
                        }}}
                con.commit();
                return true;
            }catch(Exception e){
                con.rollback();
                setLastError(e);
                return false;
            }finally{
                con.setAutoCommit(true);
            }
        } catch(SQLException e) {
            setLastError(e);
            return false;
        }
    }
    public static boolean saveAdminLecturers(List<AdminLecturerRecord> rows, List<Integer> deletedIds) {
        try(Connection con=getConnection()){
            con.setAutoCommit(false);
            try{
                for(Integer id : deletedIds) {
                    deleteLecturer(con, id);
                }

                for(AdminLecturerRecord row : rows){
                    if(row.isBlank()){
                        continue;
                    }
                    validateRequired(row.fullName, "Lecturer full name");
                    validateRequired(row.department, "Lecturer department");
                    validateRequired(row.email, "Lecturer email");
                    int departmentId=getOrCreateDepartmentId(con, row.department);
                    int lecturerId;

                    if (row.id > 0){
                        lecturerId=row.id;
                        String update="UPDATE lecturers SET full_name = ?, email = ?, mobile_number = ?, department_id = ? WHERE id = ?";
                        try (PreparedStatement ps = con.prepareStatement(update)) {
                            ps.setString(1, row.fullName);
                            ps.setString(2, row.email);
                            ps.setString(3, row.mobile);
                            ps.setInt(4, departmentId);
                            ps.setInt(5, row.id);
                            ps.executeUpdate();
                        }
                    } else{
                        int userId=createUser(con, row.fullName, "Lecturer");
                        String insert="INSERT INTO lecturers (user_id, full_name, email, mobile_number, department_id) VALUES (?, ?, ?, ?, ?)";
                        try(PreparedStatement ps=con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                            ps.setInt(1, userId);
                            ps.setString(2, row.fullName);
                            ps.setString(3, row.email);
                            ps.setString(4, row.mobile);
                            ps.setInt(5, departmentId);
                            ps.executeUpdate();
                            lecturerId=getGeneratedId(ps);
                        }
                    }
                    assignCoursesToLecturer(con, lecturerId, row.coursesTeaching);
                }
                con.commit();
                return true;
            } catch (Exception e){
                con.rollback();
                setLastError(e);
                return false;
            } finally{
                con.setAutoCommit(true);
            }
        } catch(SQLException e){
            setLastError(e);
            return false;
        }
    }
    public static boolean saveAdminCourses(List<AdminCourseRecord> rows, List<Integer> deletedIds) {
        try(Connection con=getConnection()) {
            con.setAutoCommit(false);
            try{
                for(Integer id : deletedIds){
                    try(PreparedStatement ps=con.prepareStatement("DELETE FROM courses WHERE id = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }

                for (AdminCourseRecord row : rows){
                    if (row.isBlank()){
                        continue;}
                    validateRequired(row.courseCode, "Course code");
                    validateRequired(row.courseName, "Course name");
                    validateRequired(row.lecturer, "Lecturer");
                    int lecturerId=requireLecturerId(con, row.lecturer);

                    if (row.id > 0){
                        String update="UPDATE courses SET course_code = ?, course_name = ?, credits = ?, lecturer_id = ? WHERE id = ?";
                        try(PreparedStatement ps = con.prepareStatement(update)) {
                            ps.setString(1, row.courseCode);
                            ps.setString(2, row.courseName);
                            ps.setInt(3, row.credits);
                            ps.setInt(4, lecturerId);
                            ps.setInt(5, row.id);
                            ps.executeUpdate();
                        }
                    }else{
                        String insert="INSERT INTO courses (course_code, course_name, credits, lecturer_id) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ps = con.prepareStatement(insert)) {
                            ps.setString(1, row.courseCode);
                            ps.setString(2, row.courseName);
                            ps.setInt(3, row.credits);
                            ps.setInt(4, lecturerId);
                            ps.executeUpdate();
                        }
                    }
                }
                con.commit();
                return true;}
            catch (Exception e) {
                con.rollback();
                setLastError(e);
                return false;}
            finally{con.setAutoCommit(true);}
        }catch (SQLException e) {
            setLastError(e);
            return false;}}
    public static boolean saveAdminDepartments(List<AdminDepartmentRecord> rows, List<Integer> deletedIds) {
        try (Connection con= getConnection()) {
            con.setAutoCommit(false);
            try{
                for(Integer id : deletedIds){
                    try(PreparedStatement ps=con.prepareStatement("DELETE FROM departments WHERE id = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }

                for(AdminDepartmentRecord row : rows) {
                    if(row.isBlank()) {
                        continue;
                    }
                    validateRequired(row.name, "Department name");
                    Integer hodId=optionalLecturerId(con, row.hod);
                    int departmentId;

                    if (row.id > 0) {
                        departmentId=row.id;
                        String update="UPDATE departments SET name = ?, hod_lecturer_id = ?, no_of_staff = ? WHERE id = ?";
                        try (PreparedStatement ps = con.prepareStatement(update)) {
                            ps.setString(1, row.name);
                            if (hodId == null) {
                                ps.setNull(2, Types.INTEGER);
                            } else {
                                ps.setInt(2, hodId);
                            }
                            ps.setInt(3, row.noOfStaff);
                            ps.setInt(4, row.id);
                            ps.executeUpdate();
                        }
                    } else {
                        String insert="INSERT INTO departments (name, hod_lecturer_id, no_of_staff) VALUES (?, ?, ?)";
                        try (PreparedStatement ps=con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                            ps.setString(1, row.name);
                            if (hodId == null) {
                                ps.setNull(2, Types.INTEGER);
                            } else {
                                ps.setInt(2, hodId);
                            }
                            ps.setInt(3, row.noOfStaff);
                            ps.executeUpdate();
                            departmentId = getGeneratedId(ps);
                        }
                    }
                    assignDegreesToDepartment(con, departmentId, row.degree);
                }
                con.commit();
                return true;
            } catch(Exception e){
                con.rollback();
                setLastError(e);
                return false;
            }finally{con.setAutoCommit(true);
            }
        }catch(SQLException e){setLastError(e);
            return false;
        }
    }
    public static boolean saveAdminDegrees(List<AdminDegreeRecord> rows, List<Integer> deletedIds) {
        try(Connection con= getConnection()){
            con.setAutoCommit(false);
            try{
                for(Integer id : deletedIds){
                    try(PreparedStatement ps=con.prepareStatement("DELETE FROM degrees WHERE id = ?")) {
                        ps.setInt(1, id);
                        ps.executeUpdate();
                    }
                }

                for(AdminDegreeRecord row : rows){
                    if (row.isBlank()){
                        continue;
                    }
                    validateRequired(row.degree, "Degree name");
                    validateRequired(row.department, "Department");
                    int departmentId=getOrCreateDepartmentId(con, row.department);
                    if(row.id > 0){
                        String update= "UPDATE degrees SET name = ?, department_id = ?, no_of_students = ? WHERE id = ?";
                        try(PreparedStatement ps = con.prepareStatement(update)){
                            ps.setString(1, row.degree);
                            ps.setInt(2, departmentId);
                            ps.setInt(3, row.noOfStudents);
                            ps.setInt(4, row.id);
                            ps.executeUpdate();
                        }
                    }else{
                        String insert="INSERT INTO degrees (name, department_id, no_of_students) VALUES (?, ?, ?)";
                        try(PreparedStatement ps = con.prepareStatement(insert)){
                            ps.setString(1, row.degree);
                            ps.setInt(2, departmentId);
                            ps.setInt(3, row.noOfStudents);
                            ps.executeUpdate();}
                    }
                }
                con.commit();
                return true;
            }catch(Exception e){
                con.rollback();
                setLastError(e);
                return false;
            }finally{
                con.setAutoCommit(true);
            }
        }catch(SQLException e){
            setLastError(e);
            return false;
        }
    }
    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
    private static void validateRequired(String value, String label) throws SQLException {
        if(safe(value).isEmpty()){
            throw new SQLException(label + " cannot be empty.");
        }
    }
    private static int getGeneratedId(PreparedStatement ps) throws SQLException {
        try(ResultSet keys = ps.getGeneratedKeys()){
            if(keys.next()){
                return keys.getInt(1);
            }
        }
        throw new SQLException("Could not read generated database ID.");
    }
    private static int createUser(Connection con, String baseUsername, String role) throws SQLException {
        String base = safe(baseUsername);
        if(base.isEmpty()) {
            base = role.toLowerCase();
        }
        String username = base;
        int suffix = 1;
        while (usernameExists(con, username)) {
            username = base + suffix;
            suffix++;
        }
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try(PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, "1234"); // default password for users created by admin
            ps.setString(3, role);
            ps.executeUpdate();
            return getGeneratedId(ps);
        }
    }

    private static boolean usernameExists(Connection con, String username) throws SQLException {
        try(PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE username = ?")) {
            ps.setString(1, username);
            try(ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    private static int requireDegreeId(Connection con, String degreeName) throws SQLException {
        Integer id= findDegreeId(con, degreeName);
        if(id ==null) {
            throw new SQLException("Degree not found: " + degreeName + ". Add it in the Degrees page first.");
        }
        return id;
    }
    private static Integer findDegreeId(Connection con, String degreeName) throws SQLException {
        try(PreparedStatement ps = con.prepareStatement("SELECT id FROM degrees WHERE name = ?")) {
            ps.setString(1, safe(degreeName));
            try(ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }
    private static int getOrCreateDepartmentId(Connection con, String departmentName) throws SQLException {
        validateRequired(departmentName, "Department");
        try(PreparedStatement ps = con.prepareStatement("SELECT id FROM departments WHERE name = ?")) {
            ps.setString(1, safe(departmentName));
            try(ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    return rs.getInt("id");
                }
            }
        }
        try(PreparedStatement ps = con.prepareStatement(
                "INSERT INTO departments (name, hod_lecturer_id, no_of_staff) VALUES (?, NULL, 0)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, safe(departmentName));
            ps.executeUpdate();
            return getGeneratedId(ps);
        }
    }
    private static int requireLecturerId(Connection con, String lecturerName) throws SQLException {
        Integer id= optionalLecturerId(con, lecturerName);
        if(id == null){
            throw new SQLException("Lecturer not found: " + lecturerName + ". Add the lecturer first.");
        }
        return id;
    }
    private static Integer optionalLecturerId(Connection con, String lecturerName) throws SQLException {
        String name = safe(lecturerName);
        if(name.isEmpty() || "-".equals(name)) {
            return null;
        }
        try(PreparedStatement ps = con.prepareStatement("SELECT id FROM lecturers WHERE full_name = ? ORDER BY id LIMIT 1")) {
            ps.setString(1, name);
            try(ResultSet rs = ps.executeQuery()){
                return rs.next() ? rs.getInt("id") : null;
            }
        }
    }
    private static void deleteStudent(Connection con, int studentId) throws SQLException {
        Integer userId=null;
        try(PreparedStatement ps=con.prepareStatement("SELECT user_id FROM students WHERE id = ?")) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if(rs.next()){
                    userId = rs.getInt("user_id");
                }
            }
        }
        try(PreparedStatement ps = con.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, studentId);
            ps.executeUpdate();
        }
        if(userId != null){
            try(PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE id = ?")){
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        }}
    private static void deleteLecturer(Connection con, int lecturerId) throws SQLException{
        Integer userId = null;
        try(PreparedStatement ps = con.prepareStatement("SELECT user_id FROM lecturers WHERE id = ?")) {
            ps.setInt(1, lecturerId);
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    userId=rs.getInt("user_id");
                }
            }
        }
        try(PreparedStatement ps = con.prepareStatement("DELETE FROM lecturers WHERE id = ?")){
            ps.setInt(1, lecturerId);
            ps.executeUpdate();
        }
        if(userId != null){
            try(PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE id = ?")){
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        }
    }
    private static void assignCoursesToLecturer(Connection con, int lecturerId, String courseCodes) throws SQLException {
        String codes=safe(courseCodes);
        if(codes.isEmpty()){
            return;
        }
        String[] parts = codes.split(",");
        for(String part : parts){
            String code = safe(part);
            if(code.isEmpty()){
                continue;
            }
            try(PreparedStatement ps = con.prepareStatement("UPDATE courses SET lecturer_id = ? WHERE course_code = ?")) {
                ps.setInt(1, lecturerId);
                ps.setString(2, code);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Course code not found: " + code + ". Add it in the Courses page first.");
                }
            }
        }
    }
    private static void assignDegreesToDepartment(Connection con, int departmentId, String degreeNames) throws SQLException {
        String names = safe(degreeNames);
        if(names.isEmpty()){
            return;
        }
        String[] parts = names.split(",");
        for(String part : parts) {
            String degreeName = safe(part);
            if(degreeName.isEmpty()){
                continue;
            }
            Integer degreeId = findDegreeId(con, degreeName);
            if(degreeId == null){
                try(PreparedStatement ps = con.prepareStatement("INSERT INTO degrees (name, department_id, no_of_students) VALUES (?, ?, 0)")) {
                    ps.setString(1, degreeName);
                    ps.setInt(2, departmentId);
                    ps.executeUpdate();
                }
            }else{
                try(PreparedStatement ps = con.prepareStatement("UPDATE degrees SET department_id = ? WHERE id = ?")) {
                    ps.setInt(1, departmentId);
                    ps.setInt(2, degreeId);
                    ps.executeUpdate();
                }
            }
        }
    }
}
