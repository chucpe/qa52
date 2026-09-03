package pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import static com.codeborne.selenide.Selenide.$;

public class PaymentPage {

    public PaymentPage verifyPaymentFormOpened() {
        String currentUrl = WebDriverRunner.url();
        System.out.println("   Текущий URL: " + currentUrl);
        return this;
    }
}