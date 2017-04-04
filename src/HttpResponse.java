import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chenlin on 27/03/2017.
 */
public class HttpResponse {
    private HttpURLConnection connection = null;
    private HashMap<String, String> headers = null;
    private HashMap<String, String> cookies = null;
    private String body = null;

    public HttpResponse(HttpURLConnection connection) throws IOException {
        this.connection = connection;
        getBody();
        getHeaders();
        getCookies();
    }

    public String text () {
        return this.body;
    }

    public HashMap<String, String> headers () {
        return this.headers;
    }

    public HashMap<String, String> cookies() {
        return this.cookies;
    }

    /**
     * get cookies from connection
     */
    private void getCookies() {
        this.cookies = new HashMap<>();
        if (!headers.containsKey("Set-Cookie")) {
            return ;
        }
        String setCookie = headers.get("Set-Cookie");
        String[] cookies = setCookie.substring(1, setCookie.length() - 1).split(";");
        for (String cookieStr : cookies) {
            String[] cookie = cookieStr.split("=");
            this.cookies.put(cookie[0].trim(), cookie[1].trim());
        }
    }

    /**
     * get headers from connection
     */
    private void getHeaders() {
        this.headers = new HashMap<>();
        Map<String, List<String>> map = this.connection.getHeaderFields();
        if (map == null) {
            return ;
        }
        for (String key : map.keySet()) {
            this.headers.put(key, map.get(key).toString());
        }
    }

    /**
     * get body from connection
     */
    private void getBody() throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), Charset.forName("gb2312")));
        StringBuilder tmp = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            tmp.append(line + "\n");
        }
        body = new String(tmp);
        in.close();
    }
}
