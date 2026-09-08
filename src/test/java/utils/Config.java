package utils;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static com.codeborne.selenide.Selenide.open;

public class Config {

    @BeforeEach
    public void setUp() {
        setup();
    }

    @AfterEach
    public void tearDown() {
        Selenide.closeWebDriver();
    }

    public static void setup() {
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;
        Configuration.pageLoadTimeout = 30000;
        Configuration.headless = false;
        Configuration.savePageSource = false;
        Configuration.screenshots = true;
        Configuration.clickViaJs = false;
    }

    public static void openPage(String url) {
        open(url);
    }
}