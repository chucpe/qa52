package utils;

import com.codeborne.selenide.Configuration;

public class Config {

    public static void setup() {
        // Настройки браузера
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.headless = false;          // true - если нужно без GUI
        Configuration.timeout = 15000;           // 15 секунд ожидания
        Configuration.baseUrl = "https://www.mts.by";
        Configuration.pageLoadTimeout = 30000;   // 30 секунд на загрузку страницы

        // Настройки для ускорения тестов
        Configuration.holdBrowserOpen = false;   // Закрывать браузер после тестов
        Configuration.screenshots = true;        // Делать скриншоты при падении
        Configuration.savePageSource = true;     // Сохранять HTML при падении
    }
}