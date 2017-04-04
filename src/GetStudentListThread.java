import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chenlin on 29/03/2017.
 */
public class GetStudentListThread extends Thread {
    private static final String URL = "http://192.168.2.229/newkc/kcxkrs.asp";
    private static final String tableReg = "<table border=1 bgColor=white cellSpacing=0 cellPadding=0 align=center background=\"\" borderColorDark=#ffffff borderColorLight=#000000 width=\"660\" height=\"42\">([\\w\\W]*?)</table>";
    private static final String trReg = "<tr>([\\w\\W]*?)</tr>";

    private String courseNum;
    private int courseId;

    CountDownLatch latch;

    public GetStudentListThread(String courseNum, int courseId, CountDownLatch latch) {
        this.courseNum = courseNum;
        this.courseId = courseId;
        this.latch = latch;
    }

    public void run() {
//        System.out.println("正在获取课程号:" + courseNum + " 学生列表...");

        HttpRequest request = new HttpRequest(URL, "ykch", courseNum);
        request.addCookies(Main.cookies);

        String html;
        try {
            HttpResponse response = request.get();
            html = response.text();
        } catch (IOException e) {
            e.printStackTrace();
            latch.countDown();
            return ;
        }

        Pattern pattern = Pattern.compile(tableReg);
        Matcher matcher = pattern.matcher(html);
        // 调用两次find方法，过滤掉第一个匹配的table信息
        String trsHtml;
        if (matcher.find() && matcher.find()) {
            trsHtml = matcher.group(1);
        } else {
            System.out.println(html);
            System.err.println("匹配table数据失败, 课程号为: " + courseNum);
            latch.countDown();
            return ;
        }

        pattern = Pattern.compile(trReg);
        matcher = pattern.matcher(trsHtml);
        while (matcher.find()) {
            String tr = matcher.group(1);
            tr = tr.replaceAll("</?(.*?)>", ""); // 去除html标签
            tr = tr.replaceAll(" |\t", "");
            while (tr.contains("\n\n")) {
                tr = tr.replaceAll("\n\n", "\n");
            }

            String tds[] = tr.trim().split("\n");

            try {
                Student student = new Student(tds[1], tds[2], tds[3], tds[4].substring(0, tds[4].length() - 2), Integer.valueOf(tds[4].substring(tds[4].length() - 2)));
                student.saveToDatabase();
                student.addToCourse(courseId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        latch.countDown();
//        System.out.println("获取课程号:" + courseNum + " 学生列表完成");
    }
}
