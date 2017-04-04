import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenlin on 27/03/2017.
 */
public class Main {
    public static HashMap<String, String> cookies;
    final String termNum = "20162"; // 要爬取的学期号
    final String coursesListBaseUrl = "http://192.168.2.229/newkc/akcjj0.asp";
    final String course = "http://192.168.2.229/newkc/akechengdw.asp"; // 开课单位列表url;
    static ArrayList<String> collegeList;


    public static void main(String[] args) throws Exception {
        new Main();
    }

    public Main() throws Exception {
        Database.connect();

        int command = 0;
        Scanner in = new Scanner(System.in);
        System.out.println("请输入命令前的编号");
        System.out.println("1. 爬取所有选课信息");
        System.out.println("2. 查询指定学号选课信息");
        System.out.println("3. 退出程序");

        while (command != 3) {
            System.out.print("输入要执行的命令编号:");
            command = in.nextInt();
            switch (command) {
                case 1:
                    getCookie();
                    parseCollegeList();
                    CountDownLatch countDownLatch = new CountDownLatch(collegeList.size());
                    getCourseList(countDownLatch);
                    countDownLatch.await();
                    System.out.println("课程信息爬取完成！");
                    break;
                case 2:
                    System.out.print("请输入要查询的学号:");
                    String stuNum = in.next();
                    getCurriculumSchedule(stuNum);
                    break;
                case 3:
                    break;
                default :
                    System.out.println("命令输入有误, 请重新输入");
            }
        }

        Database.closeConnection();
    }

    /**
     * 获取cookie
     */
    private void getCookie() throws Exception {
        HttpRequest cookieRequest = new HttpRequest(coursesListBaseUrl, "xqh", termNum);
        this.cookies = cookieRequest.get().cookies();
    }

    /**
     * 获取开课单位列表
     */
    private void parseCollegeList() throws Exception {
        HttpRequest request = new HttpRequest(course);
        request.addCookies(this.cookies);
        String html = request.get().text();

        String reg = "<option value=\"(.*)\">";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(html);
        collegeList = new ArrayList<>();
        while (matcher.find()) {
            collegeList.add(matcher.group(1).trim());
        }
    }

    /**
     * 启动线程爬出课程每个学院的课程列表
     */
    private void getCourseList(CountDownLatch latch) {
        for (int i = 0; i < collegeList.size(); ++i) {
            new GetCourseListThread(collegeList.get(i), latch).start();
        }
    }

    /**
     * 获取指定学号的课程列表
     * @param stuNum
     */
    private void getCurriculumSchedule(String stuNum) {
        String sql = "select student.*, courses.* " +
                "from student, courses, student__courses " +
                "where student.id = student__courses.id_stu and " +
                "courses.id = student__courses.id_course and " +
                "student.stu_num = ?";
        Connection con = Database.getConnection();
        synchronized (con) {
            try {
                PreparedStatement psmt = con.prepareStatement(sql);
                psmt.setString(1, stuNum);
                ResultSet rs = psmt.executeQuery();
                System.out.println("学号:" + stuNum + "选课信息如下:");
                while (rs.next()) {
                    System.out.println(rs.getString("stu_num") + " " +
                            rs.getString("name") + " " +
                            rs.getString("sex") + " " +
                            rs.getString("major") + " " +
                            rs.getString("class") + "班 " +
                            rs.getString("course_name") + " " +
                            rs.getString("teacher") + " " +
                            rs.getString("class_week") + " " +
                            rs.getString("class_time") + " " +
                            rs.getString("venue")
                    );
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}