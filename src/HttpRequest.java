import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by chenlin on 27/03/2017.
 */
public class HttpRequest {
    private String url;
    private HashMap<String, String> params;
    private HashMap<String, String> cookies;

    public HttpRequest(String url, String key, String value) {
        this.url = url;
        this.params = new HashMap<>();
        this.params.put(key, value);
        this.cookies = new HashMap<>();
    }

    public HttpRequest(String url, HashMap<String, String> params) {
        this.url = url;
        this.params = params;
        this.cookies = new HashMap<>();
    }

    public HttpRequest(String url) {
        this.url = url;
        this.params = new HashMap<>();
        this.cookies = new HashMap<>();
    }

    public HttpRequest addParam(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    public HttpResponse post() throws IOException {
        URL url = new URL(this.url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        setHeaders(con);
        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);

        PrintWriter pw = new PrintWriter(con.getOutputStream());
        pw.print(buildQuery());
        pw.flush();
        pw.close();

        return new HttpResponse(con);
    }

    public HttpResponse get() throws IOException {
        String params = buildQuery();
        if (params.length() > 0) {
            params = "?" + params;
        }

        URL url = new URL(this.url + params);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        setHeaders(con);
        return new HttpResponse(con);
    }

    public HttpRequest addCookie(String key, String value) {
        this.cookies.put(key, value);
        return this;
    }

    public HttpRequest addCookies(HashMap<String, String> cookies) {
        this.cookies = cookies;
        return this;
    }

    /**
     * generate query string by this.params
     * @return
     */
    private String buildQuery() throws UnsupportedEncodingException {
        StringBuilder query = new StringBuilder();
        for (String key : this.params.keySet()) {
            query.append(key + "=" + URLEncoder.encode(this.params.get(key), "gb2312") + "&");
        }
        if (query.length() > 0) {
            query.substring(0, query.length() - 1);
        }
        return new String(query);
    }

    /**
     * set connection headers
     */
    private void setHeaders(HttpURLConnection connection) {
        // set default headers
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // set cookies
        StringBuilder cookiesStr = new StringBuilder();
        for (String key : this.cookies.keySet()) {
            cookiesStr.append(key + "=" + this.cookies.get(key));
        }
        connection.setRequestProperty("Cookie", new String(cookiesStr));
    }
}
