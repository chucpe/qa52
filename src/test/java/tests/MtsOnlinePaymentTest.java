package tests;

import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.HomePage;
import pages.PaymentFormPage;
import utils.Config;

import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;

public class MtsOnlinePaymentTest {

    private HomePage homePage;
    private PaymentFormPage paymentFormPage;

    @BeforeEach
    void setUp() {
        Config.setup();
        homePage = new HomePage();
        homePage.openPage();

        $("div.pay__wrapper").shouldBe(Condition.visible, Duration.ofSeconds(10));
    }

    @Test
    @DisplayName("Проверка блока 'Онлайн пополнение без комиссии'")
    void testOnlinePaymentBlock() {
        homePage.verifyBlockTitle();
        homePage.verifyPaymentLogosExist();
        homePage.clickMoreDetailsAndVerify();

        homePage
                .selectPaymentOption("Услуги связи")
                .verifyPlaceholders(
                        "Номер телефона",
                        "Сумма",
                        "E-mail для отправки чека"
                );

        homePage
                .fillPhoneNumber("297777777")
                .fillAmount("10")
                .fillEmail("test@test.com");

        paymentFormPage = homePage.clickContinue();
        paymentFormPage.verifyPaymentFormOpened();
    }

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
