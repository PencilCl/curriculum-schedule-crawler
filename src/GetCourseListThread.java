import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenlin on 29/03/2017.
 */
public class GetCourseListThread extends Thread {
    private static final String URL = "http://192.168.2.229/newkc/kccx.asp?flag=kkdw";
    private static final String tableReg = "<table border=1 bgColor=white cellSpacing=0 cellPadding=0 align=center background=\"\" borderColorDark=#ffffff borderColorLight=#00ff00 width=\"960\" height=\"42\">([\\w\\W]*?)</table>";
    private static final String trReg = "<tr>([\\w\\W]*?)</tr>";
    private String collegeName;
    ArrayList<Course> courses;

    CountDownLatch latch;

    public GetCourseListThread(String collegeName, CountDownLatch latch) {
        this.collegeName = collegeName;
        this.latch = latch;
        this.courses = new ArrayList<>();
    }

    public void run() {
        System.out.println("正在获取开课单位:" + collegeName + " 课程列表...");

        parseCourseList();
        CountDownLatch latch = new CountDownLatch(courses.size());
        getStudents(latch);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("获取开课单位 " + collegeName + " 课程列表完成");
        this.latch.countDown();
    }

    /**
     * 获取当前开课单位的课程列表
     */
    private void parseCourseList() {
        HttpRequest request = new HttpRequest(URL, "bh", collegeName);
        request.addCookies(Main.cookies);

        String html = null;
        try {
            HttpResponse response = request.post();
            html = response.text();
        } catch (IOException e) {
            e.printStackTrace();
            latch.countDown();
            return ;
        }

        Pattern pattern = Pattern.compile(tableReg);
        Matcher matcher = pattern.matcher(html);
        String trsHtml;
        if (matcher.find()) {
            trsHtml = matcher.group(1);
        } else {
            System.out.println(html);
            System.err.println("匹配table数据失败, 开课单位为: " + collegeName);
            latch.countDown();
            return ;
        }

        pattern = Pattern.compile(trReg);
        matcher = pattern.matcher(trsHtml);
        matcher.find(); //  跳过第一个结果(表格头信息)
        while (matcher.find()) {
            String tr = matcher.group(1);
            tr = tr.replaceAll("</?(.*?)>", ""); // 去除html标签
            tr = tr.replaceAll(" |\t", "");
            while (tr.contains("\n\n")) {
                tr = tr.replaceAll("\n\n", "\n");
            }

            String[] tds = tr.trim().split("\n");
            if (tds.length == 15) {
                Course course = new Course(tds[1], tds[2], tds[9], tds[10], tds[11], tds[12]);
                courses.add(course);
            }
        }
    }

    private void getStudents(CountDownLatch latch) {
        for (int i = 0; i < courses.size(); ++i) {
            Course course = courses.get(i);
            try {
                course.saveToDatabase();
                course.getStudents(latch);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
