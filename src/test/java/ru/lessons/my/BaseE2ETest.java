package ru.lessons.my;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import ru.lessons.my.bot.TelegramBot;
import ru.lessons.my.config.BaseTestConfig;
import ru.lessons.my.config.BotConfig;
import ru.lessons.my.config.SecurityConfig;
import ru.lessons.my.config.TestDbConfig;
import ru.lessons.my.config.WebConfig;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


//todo Заложился на использование одного контейнера с БД для всех тестов, но для E2E не работает откат транзакции,
// так как они выполняются в другом потоке. Возможно стоит пересоздавать контейнер с БД для каждого теста.
@Slf4j
@SpringJUnitConfig(classes = {BaseTestConfig.class, WebConfig.class, TestDbConfig.class, SecurityConfig.class})
@MockitoBean(types = {TelegramBot.class, BotConfig.class})
@WebAppConfiguration
public abstract class BaseE2ETest {

    protected static Tomcat tomcat;
    protected static int port;

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    private static boolean tomcatStarted = false;

    @BeforeAll
    static void setUpPlaywright() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void tearDown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUpTomcatAndBrowser() throws Exception {
        if (!tomcatStarted) {
            startEmbeddedTomcat();
            tomcatStarted = true;
        }
        context = browser.newContext();
        context.onRequest(request -> log.debug("→ {} {}", request.method(), request.url()));
        context.onResponse(response -> log.debug("← {} {}", response.status(), response.url()));
        page = context.newPage();
    }

    @AfterEach
    void closeBrowserContext() {
        if (page != null && !page.isClosed()) {
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(Paths.get("build/test-results/screenshots/" +
                                       LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ".png")));
        }
        if (context != null) {
            context.close();
        }
    }

    private void startEmbeddedTomcat() throws Exception {
        tomcat = new Tomcat();
        tomcat.setPort(0);

        String docBase = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
        Context tomcatContext = tomcat.addContext("", docBase);

        DispatcherServlet dispatcherServlet = new DispatcherServlet(webApplicationContext);

        Tomcat.addServlet(tomcatContext, "dispatcher", dispatcherServlet);
        tomcatContext.addServletMappingDecoded("/", "dispatcher");

        DelegatingFilterProxy securityFilter = new DelegatingFilterProxy(
                "springSecurityFilterChain",
                webApplicationContext
        );

        FilterDef secFilterDef = new FilterDef();
        secFilterDef.setFilterName("springSecurityFilterChain");
        secFilterDef.setFilter(securityFilter);
        tomcatContext.addFilterDef(secFilterDef);

        FilterMap secFilterMap = new FilterMap();
        secFilterMap.setFilterName("springSecurityFilterChain");
        secFilterMap.addURLPattern("/*");
        tomcatContext.addFilterMap(secFilterMap);

        tomcat.start();

        port = tomcat.getConnector().getLocalPort();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                tomcat.stop();
                tomcat.destroy();
            } catch (LifecycleException e) {
                log.error(e.getMessage(), e);
            }
        }));
    }

    protected String url(String path) {
        return "http://localhost:" + port + path;
    }

    protected void login() {
        page.navigate(url("/login"));
        page.fill("#username", "manager1");
        page.fill("#password", "password");
        page.click("button[type='submit']");

        page.waitForURL("**/enterprises");
    }

}
