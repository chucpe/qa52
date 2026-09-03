package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import utils.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.HomePage;
import pages.PaymentFormPage;
import utils.Config;

@Epic("Тестирование сайта mts.by")
@Feature("Онлайн пополнение без комиссии")
public class MtsOnlinePaymentTest {

    private HomePage homePage;
    private PaymentFormPage paymentFormPage;

    @BeforeEach
    void setUp() {
        Config.setup();
        homePage = new HomePage();
        homePage.openPage();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ==================== ОСНОВНЫЕ ТЕСТЫ ====================

    @Test
    @DisplayName("Проверка блока 'Онлайн пополнение без комиссии'")
    @Title("Проверка блока онлайн пополнения")
    @Description("Полная проверка блока: заголовок, логотипы, ссылка, плейсхолдеры, заполнение полей и переход к оплате")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Основной сценарий оплаты")
    void testOnlinePaymentBlock() {
        // 1. Проверяем название блока
        homePage.verifyBlockTitle();

        // 2. Проверяем наличие логотипов платёжных систем
        homePage.verifyPaymentLogosExist();

        // 3. Проверяем работу ссылки «Подробнее о сервисе»
        homePage.clickMoreDetailsAndVerify();

        // 4. Выбираем "Услуги связи" и проверяем плейсхолдеры
        homePage
                .selectPaymentOption("Услуги связи")
                .verifyPlaceholders(
                        "Номер телефона",
                        "Сумма",
                        "E-mail для отправки чека"
                );

        // 5. Заполняем поля
        homePage
                .fillPhoneNumber("297777777")
                .fillAmount("10")
                .fillEmail("test@test.com");

        // 6. Нажимаем "Продолжить"
        paymentFormPage = homePage.clickContinue();

        // 7. Проверяем форму оплаты
        paymentFormPage.verifyPaymentFormOpened();
    }

    // ==================== ТЕСТЫ ДЛЯ ПРОВЕРКИ ПЛЕЙСХОЛДЕРОВ ====================

    @Test
    @DisplayName("Проверка надписей в полях для 'Услуги связи'")
    @Title("Плейсхолдеры для Услуги связи")
    @Description("Проверка плейсхолдеров в полях формы для услуги 'Услуги связи'")
    @Severity(SeverityLevel.NORMAL)
    @Story("Проверка плейсхолдеров")
    void testServicesPlaceholders() {
        homePage
                .selectPaymentOption("Услуги связи")
                .verifyPlaceholders(
                        "Номер телефона",
                        "Сумма",
                        "E-mail для отправки чека"
                );
    }

    @Test
    @DisplayName("Проверка надписей в полях для 'Домашний интернет'")
    @Title("Плейсхолдеры для Домашний интернет")
    @Description("Проверка плейсхолдеров в полях формы для услуги 'Домашний интернет'")
    @Severity(SeverityLevel.NORMAL)
    @Story("Проверка плейсхолдеров")
    void testInternetPlaceholders() {
        homePage
                .selectPaymentOption("Домашний интернет")
                .verifyPlaceholders(
                        "Номер абонента",
                        "Сумма",
                        "E-mail для отправки чека"
                );
    }

    @Test
    @DisplayName("Проверка надписей в полях для 'Рассрочка'")
    @Title("Плейсхолдеры для Рассрочка")
    @Description("Проверка плейсхолдеров в полях формы для услуги 'Рассрочка'")
    @Severity(SeverityLevel.NORMAL)
    @Story("Проверка плейсхолдеров")
    void testInstalmentPlaceholders() {
        homePage
                .selectPaymentOption("Рассрочка")
                .verifyPlaceholders(
                        "Номер счета на 44",
                        "Сумма",
                        "E-mail для отправки чека"
                );
    }

    @Test
    @DisplayName("Проверка надписей в полях для 'Задолженность'")
    @Title("Плейсхолдеры для Задолженность")
    @Description("Проверка плейсхолдеров в полях формы для услуги 'Задолженность'")
    @Severity(SeverityLevel.NORMAL)
    @Story("Проверка плейсхолдеров")
    void testArrearsPlaceholders() {
        homePage
                .selectPaymentOption("Задолженность")
                .verifyPlaceholders(
                        "Номер счета на 2073",
                        "Сумма",
                        "E-mail для отправки чека"
                );
    }
}