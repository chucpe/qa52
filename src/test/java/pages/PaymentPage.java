package pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;

public class PaymentPage {

    private final SelenideElement paymentForm = $("#pay-connection");

    public PaymentPage verifyPaymentFormOpened() {
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(10));

        String currentUrl = WebDriverRunner.url();
        assert currentUrl != null && !currentUrl.isEmpty() : "URL is empty";

        wait.until(ExpectedConditions.visibilityOf(paymentForm));
        paymentForm.shouldBe(visible);

        String bodyText = $("body").getText();
        assert !bodyText.contains("404") && !bodyText.contains("Not Found") && !bodyText.contains("Ошибка")
                : "Page returned error";

        return this;
    }
}