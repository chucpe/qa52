package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.HomePage;
import pages.PaymentFormPage;
import utils.Config;

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

    @Test
    @DisplayName("Проверка блока 'Онлайн пополнение без комиссии'")
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

        // 6. Нажимаем "Продолжить" (проверяем, что кнопка работает)
        paymentFormPage = homePage.clickContinue();

        // 7. Проверяем, что форма оплаты открылась (с гибкой проверкой)
        paymentFormPage.verifyPaymentFormOpened();
    }

    // ==================== ТЕСТЫ ДЛЯ ПРОВЕРКИ ПЛЕЙСХОЛДЕРОВ ====================

    @Test
    @DisplayName("Проверка надписей в полях для 'Услуги связи'")
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