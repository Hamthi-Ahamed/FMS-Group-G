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
        return lastError == null || lastError.trim().isEmpty() ? "Unknown database error." : lastError;
    }

    private static void setLastError(Exception e) {
        lastError = e.getMessage();
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
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
        try (Connection con = getConnection();
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
             PreparedStatement ps = con.prepareStatement(sql)) {

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
        String sql = "UPDATE students SET email = ?, mobile_number = ? WHERE id = ?";
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
        String sql = "SELECT t.day, t.time_slot, c.course_name " +
                "FROM timetable t " +
                "JOIN courses c ON t.course_id = c.id " +
                "JOIN enrollments e ON e.course_id = c.id " +
                "JOIN students s ON s.id = e.student_id " +
                "JOIN users u ON u.id = s.user_id " +
                "WHERE u.username = ? " +
                "ORDER BY t.time_slot, FIELD(t.day,'Monday','Tuesday','Wednesday','Thursday','Friday')";

        LinkedHashMap<String, String[]> map = new LinkedHashMap<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String slot = rs.getString("time_slot");
                String day = rs.getString("day");
                String name = rs.getString("course_name");
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

        String[][] rows = new String[map.size()][6];
        int r = 0;
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            rows[r][0] = entry.getKey();
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
            ResultSet rs = ps.executeQuery();
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
            this.fullName = fullName;
            this.studentId = studentId;
            this.degree = degree;
            this.email = email;
            this.mobile = mobile;
        }
    }


    public static Object[][] getAdminStudentRows() {
        String sql = "SELECT s.id, s.full_name, s.student_id, d.name AS degree, " +
                "s.email, s.mobile_number " +
                "FROM students s " +
                "JOIN degrees d ON d.id = s.degree_id " +
                "ORDER BY s.id";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        String sql = "SELECT l.id, l.full_name, d.name AS department, " +
                "COALESCE(GROUP_CONCAT(c.course_code ORDER BY c.id SEPARATOR ', '), '') AS courses_teaching, " +
                "l.email, l.mobile_number " +
                "FROM lecturers l " +
                "JOIN departments d ON d.id = l.department_id " +
                "LEFT JOIN courses c ON c.lecturer_id = l.id " +
                "GROUP BY l.id, l.full_name, d.name, l.email, l.mobile_number " +
                "ORDER BY l.id";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        String sql = "SELECT deg.id, deg.name AS degree, d.name AS department, deg.no_of_students " +
                "FROM degrees deg " +
                "JOIN departments d ON d.id = deg.department_id " +
                "ORDER BY deg.id";
        List<Object[]> rows = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
            this.fullName = safe(fullName);
            this.department = safe(department);
            this.coursesTeaching = safe(coursesTeaching);
            this.email = safe(email);
            this.mobile = safe(mobile);
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
            this.id = id;
            this.courseCode = safe(courseCode);
            this.courseName = safe(courseName);
            this.credits = credits;
            this.lecturer = safe(lecturer);
        }

        public boolean isBlank() {
            return courseCode.isEmpty() && courseName.isEmpty() && lecturer.isEmpty();
        }
    }

    public static class AdminDepartmentRecord {
        public final int id;
        public final String name;
        public final String hod;
        public final String degree;
        public final int noOfStaff;

        public AdminDepartmentRecord(int id, String name, String hod, String degree, int noOfStaff) {
            this.id = id;
            this.name = safe(name);
            this.hod = safe(hod);
            this.degree = safe(degree);
            this.noOfStaff = noOfStaff;
        }

        public boolean isBlank() {
            return name.isEmpty() && hod.isEmpty() && degree.isEmpty();
        }
    }

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


}