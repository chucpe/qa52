package tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.HomePage;
import utils.Config;

public class MtsOnlinePaymentTest {

    private HomePage homePage;

    @BeforeEach
    void setUp() {
        Config.setup();
        homePage = new HomePage();
        homePage.openPage();
        try { Thread.sleep(3000); } catch (InterruptedException e) {}
    }

    @Test
    @DisplayName("Проверка блока 'Онлайн пополнение без комиссии'")
    void testOnlinePaymentBlock() {
        homePage.verifyBlockTitle();
        homePage.verifyServicesTabExists();
        homePage.verifyPaymentLogosExist();
        homePage.clickMoreDetailsAndVerify();

        homePage
                .selectServicesTab()
                .fillPhoneNumber("297777777")
                .fillAmount("10")
                .fillEmail("test@test.com")
                .clickContinue()
                .verifyPaymentFormOpened();
    }

    @Test
    @DisplayName("Проверка валидации при пустом номере")
    void testEmptyPhoneValidation() {
        homePage.selectServicesTab().clickContinue();
    }

    @AfterEach
    void tearDown() {
        System.out.println("Тест завершен.");
    }
}