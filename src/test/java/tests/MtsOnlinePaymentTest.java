package tests;

import pages.HomePage;
import pages.PaymentPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.codeborne.selenide.Configuration;

public class MtsOnlinePaymentTest {

    private HomePage homePage;

    @BeforeEach
    void setup() {
        Configuration.browser = "chrome";
        Configuration.timeout = 10000;
        Configuration.pollingInterval = 200;

        homePage = new HomePage();
        homePage.openPage();
    }

    @Test
    @DisplayName("Проверка блока 'Онлайн пополнение без комиссии'")
    void testOnlinePaymentBlock() {
        homePage.verifyBlockTitle();
        homePage.verifyPaymentLogosExist();
        homePage.clickMoreDetailsAndVerify(); //
    }

    @Test
    @DisplayName("Проверка заполнения формы оплаты")
    void testFillPaymentForm() {
        homePage.selectServicesTab();
        homePage.fillPhoneNumber("291234567");
        homePage.fillAmount("10.00");
        homePage.fillEmail("test@test.com");

        PaymentPage paymentPage = homePage.clickContinue();
    }

    @Test
    @DisplayName("Проверка выбора услуги связи")
    void testSelectServices() {
        homePage.selectServicesTab();
        homePage.verifyServicesTabExists();
    }
}