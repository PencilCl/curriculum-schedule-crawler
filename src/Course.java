import java.sql.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by chenlin on 28/03/2017.
 */
public class Course {
    private int id;
    private String courseNum;
    private String courseName;
    private String teacher;
    private String classWeek;
    private String classTime;
    private String venue;

    public Course(String courseNum, String courseName, String teacher, String classWeek, String classTime, String venue) {
        this.courseNum = courseNum;
        this.courseName = courseName;
        this.teacher = teacher;
        this.classWeek = classWeek;
        this.classTime = classTime;
        this.venue = venue;
    }

    public Course(int id, String courseNum, String courseName, String teacher, String classWeek, String classTime, String venue) {
        this.id = id;
        this.courseNum = courseNum;
        this.courseName = courseName;
        this.teacher = teacher;
        this.classWeek = classWeek;
        this.classTime = classTime;
        this.venue = venue;
    }

    /**
     * 保存课程信息到数据库
     * @return 保存成功返回true 反之返回false
     */
    public boolean saveToDatabase() throws SQLException {
        Connection con = Database.getConnection();

        synchronized (con) {
            // 查询数据库中是否已存在当前课程信息 若已存在则不再重复插入
            String sql = "select id, course_name from courses where course_num=?";
            PreparedStatement psmt = con.prepareStatement(sql);
            psmt.setString(1, courseNum);
            ResultSet rs = psmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("id");
                return true;
            }

            sql = "insert into courses" +
                    "(course_num, course_name, teacher, class_week, class_time, venue) " +
                    "values(?, ?, ?, ?, ?, ?)";
            psmt = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            psmt.setString(1, courseNum);
            psmt.setString(2, courseName);
            psmt.setString(3, teacher);
            psmt.setString(4, classWeek);
            psmt.setString(5, classTime);
            psmt.setString(6, venue);

            psmt.executeUpdate();
            rs = psmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
                return true;
            }
            return false;
        }
    }

    /**
     * 获取选了当前课程的学生列表
     * 并添加到数据库中
     */
    public void getStudents(CountDownLatch latch) {
        new GetStudentListThread(courseNum, id, latch).start();
    }

    public int getId() {
        return id;
    }

    public String getCourseNum() {
        return courseNum;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getClassWeek() {
        return classWeek;
    }

    public String getClassTime() {
        return classTime;
    }

    public String getVenue() {
        return venue;
    }
}
