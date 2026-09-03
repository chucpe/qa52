package utils;

import com.codeborne.selenide.Configuration;

public class Config {

    public static void setup() {
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.headless = false;
        Configuration.timeout = 10000;
        Configuration.baseUrl = "https://www.mts.by";
        Configuration.pageLoadTimeout = 30000;
    }
}