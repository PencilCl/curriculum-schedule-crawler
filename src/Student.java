import java.sql.*;

/**
 * Created by chenlin on 30/03/2017.
 */
public class Student {
    private int id;
    private String stu_num;
    private String name;
    private String sex;
    private String major;
    private int classNum; // 班级

    public Student(int id, String stu_num, String name, String sex, String major, int classNum) {
        this.id = id;
        this.stu_num = stu_num;
        this.name = name;
        this.sex = sex.equals("女") ? "female" : "male";
        this.major = major;
        this.classNum = classNum;
    }

    public Student(String stu_num, String name, String sex, String major, int classNum) {
        this.stu_num = stu_num;
        this.name = name;
        this.sex = sex.equals("女") ? "female" : "male";
        this.major = major;
        this.classNum = classNum;
    }

    public boolean saveToDatabase() throws SQLException {
        Connection con = Database.getConnection();

        synchronized (con) {
            // 如果存在当前学生则不重复插入
            String sql = "select id from student where stu_num=?";
            PreparedStatement psmt = con.prepareStatement(sql);
            psmt.setString(1, stu_num);
            ResultSet rs = psmt.executeQuery();
            if (rs.next()) {
                this.id = rs.getInt("id");
                return true;
            }

            sql = "insert into student" +
                    "(stu_num, name, sex, major, class) " +
                    "values(?, ?, ?, ?, ?)";
            psmt = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            psmt.setString(1, stu_num);
            psmt.setString(2, name);
            psmt.setString(3, sex);
            psmt.setString(4, major);
            psmt.setInt(5, classNum);

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
     * 标记该学生加入课程
     * @param courseId
     */
    public boolean addToCourse(int courseId) throws SQLException {
        Connection con = Database.getConnection();

        synchronized (con) {
            String sql = "select id from student__courses where id_stu=? and id_course=?";
            PreparedStatement psmt = con.prepareStatement(sql);
            psmt.setInt(1, id);
            psmt.setInt(2, courseId);
            ResultSet rs = psmt.executeQuery();
            if (rs.next()) {
                return true;
            }

            sql = "insert into student__courses" +
                    "(id_stu, id_course) " +
                    "values(?, ?)";
            psmt = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            psmt.setInt(1, id);
            psmt.setInt(2, courseId);

            psmt.executeUpdate();
            rs = psmt.getGeneratedKeys();
            if (rs.next()) {
                return true;
            }
            return false;
        }
    }

    public int getId() {
        return id;
    }

    public String getStu_num() {
        return stu_num;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public String getMajor() {
        return major;
    }

    public int getClassNum() {
        return classNum;
    }
}
