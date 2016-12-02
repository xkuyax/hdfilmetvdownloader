package me.xkuyax.hdfilmetv.download;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.Data;
import me.xkuyax.hdfilmetv.download.download.BootstrapDownloader;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;

@Data
public class Login {

    private static final Type COOKIE_TYPE = new TypeToken<ArrayList<BasicClientCookie2>>() {}.getType();
    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:50.0) Gecko/20100101 Firefox/50.0";
    private final int WEB_CRAWLER = BootstrapDownloader.WEB_CRAWLER;
    private static final boolean VERIFY_WEB = true;
    private CloseableHttpClient httpClient;
    private CookieStore cookies;
    private final String url;
    private final String title;

    public CloseableHttpClient run() throws Exception {
        loadExistingUserAgent();
        setupClient();
        if (loadExisting()) {
            fuckCloudflare();
        }
        return httpClient;
    }

    private void loadExistingUserAgent() throws IOException {
        Path userAgentPath = Paths.get("useragent.txt");
        if (Files.exists(userAgentPath)) {
            USER_AGENT = new String(Files.readAllBytes(userAgentPath));
            System.out.println("Loaded user agent " + USER_AGENT + " from config");
        }
    }

    //cookie cache
    private boolean loadExisting() throws IOException {
        Path cookiesPath = Paths.get("cookies.txt");
        if (Files.exists(cookiesPath)) {
            ArrayList<BasicClientCookie2> cookies = new Gson().fromJson(new String(Files.readAllBytes(cookiesPath)), COOKIE_TYPE);
            cookies.forEach(cookie -> this.cookies.addCookie(cookie));
            if (VERIFY_WEB) {
                try (CloseableHttpResponse httpResponse = httpClient.execute(new HttpGet(url))) {
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        System.out.println("Using cached cookies");
                        return false;
                    }
                }
            } else {
                System.out.println("Skipping cookie verification");
                return false;
            }
        }
        return true;
    }

    private void setupClient() throws Exception {
        cookies = new BasicCookieStore();
        //ssl
        SSLContextBuilder builder = SSLContexts.custom();
        builder.loadTrustMaterial(null, (TrustStrategy) (chain, authType) -> true);
        SSLContext sslContext = builder.build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).register("http", new PlainConnectionSocketFactory()).build();
        //my code
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        cm.setMaxTotal(WEB_CRAWLER);
        cm.setDefaultMaxPerRoute(WEB_CRAWLER);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        this.httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).setSSLSocketFactory(sslsf).setDefaultCookieStore(cookies).setConnectionManager(cm).setUserAgent(USER_AGENT).build();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("username", "password"));
    }

    private void fuckCloudflare() throws IOException {
        System.out.println("trying to fuck cloud flare");
        System.getProperties().put("webdriver.gecko.driver", Paths.get("geckodriver.exe").toAbsolutePath().toString());
        FirefoxDriver webDriver = new FirefoxDriver();
        USER_AGENT = (String) webDriver.executeScript("return navigator.userAgent", "");
        webDriver.get(url);
        Wait<WebDriver> wait = new WebDriverWait(webDriver, 30);
        wait.until(input -> input != null && input.getTitle().toLowerCase().contains(title));
        Options options = webDriver.manage();
        Set<Cookie> cookieSet = options.getCookies();
        cookieSet.forEach(cookie -> {
            System.out.println(cookie.getName() + " " + cookie.getValue());
            BasicClientCookie basicClientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
            basicClientCookie.setExpiryDate(cookie.getExpiry());
            basicClientCookie.setDomain(cookie.getDomain());
            basicClientCookie.setPath(cookie.getPath());
            this.cookies.addCookie(basicClientCookie);
        });
        //cookie cache
        Files.write(Paths.get("cookies.txt"), new Gson().toJson(cookies.getCookies(), COOKIE_TYPE).getBytes());
        Files.write(Paths.get("useragent.txt"), USER_AGENT.getBytes());
        System.out.println("Got cookies and successfully fucked cloudflare :)");
        System.out.println("Saved user agent " + USER_AGENT + " to config");
        webDriver.close();
        webDriver.kill();
        System.out.println("Closed firefox");
    }
}
