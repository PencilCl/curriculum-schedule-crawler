import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by chenlin on 28/03/2017.
 */
public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/courses_crawler?useSSL=false&useUnicode=true&characterEncoding=utf-8"; // useUnicode=true&characterEncoding=utf-8 解决中文乱码问题
    private static final String NAME = "coursecrawler";
    private static final String PASSWORD = "coursecrawler";
    private static Connection con = null;

    /**
     * 连接数据库
     * @return
     */
    public static boolean connect() {
        try {
            if (con == null || con.isClosed()) {
                // 加载驱动程序
                Class.forName("com.mysql.jdbc.Driver");
                con = DriverManager.getConnection(URL, NAME, PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 获取连接
     * @return 失败返回null
     */
    synchronized public static Connection getConnection() {
        if (connect()) {
            return con;
        }
        return null;
    }

    /**
     * 创建Statement
     * @return
     */
    public static Statement createStatement() throws SQLException {
        getConnection();
        if (con != null) {
            return con.createStatement();
        }
        return null;
    }

    /**
     * 关闭连接
     */
    public static void closeConnection() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            con = null;
        }
    }
}
