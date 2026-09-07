package pages;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class HomePage {

    private final SelenideElement blockTitle = $("div.pay__wrapper h2");
    private final SelenideElement selectHeader = $(".select__header");
    private final SelenideElement selectOption = $x("//li[contains(@class, 'select__item')]//p[text()='Услуги связи']");
    private final SelenideElement phoneInput = $("#connection-phone");
    private final SelenideElement amountInput = $("#connection-sum");
    private final SelenideElement emailInput = $("#connection-email");
    private final SelenideElement continueButton = $("#pay-connection .button__default");
    private final ElementsCollection paymentLogos = $$("div.pay__partners img");
    private final SelenideElement moreDetailsLink = $("div.pay__wrapper a[href*='poryadok-oplaty']");
    private final SelenideElement connectionForm = $("#pay-connection");
    private final SelenideElement cookieBanner = $(".cookie.show");
    private final SelenideElement cookieAcceptButton = $(".cookie__ok");

    public HomePage openPage() {
        open("https://www.mts.by");
        return this;
    }

    private HomePage closeCookieBanner() {
        try {
            WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(5));
            wait.until(ExpectedConditions.elementToBeClickable(cookieAcceptButton)).click();
        } catch (TimeoutException e) {
        } catch (Exception e) {
        }
        return this;
    }

    public HomePage verifyBlockTitle() {
        blockTitle.shouldBe(visible);
        blockTitle.shouldHave(text("Онлайн пополнение"));
        return this;
    }

    public HomePage verifyServicesTabExists() {
        selectHeader.shouldBe(visible);
        return this;
    }

    public HomePage verifyPaymentLogosExist() {
        paymentLogos.shouldHave(sizeGreaterThan(0));
        paymentLogos.forEach(logo -> logo.shouldBe(visible));
        return this;
    }

    public HomePage clickMoreDetailsAndVerify() {
        closeCookieBanner();
        String currentUrl = WebDriverRunner.url();
        moreDetailsLink.shouldBe(visible).click();
        WebDriverWait wait = new WebDriverWait(WebDriverRunner.getWebDriver(), Duration.ofSeconds(5));
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(currentUrl)));
        String newUrl = WebDriverRunner.url();
        assert newUrl.contains("poryadok-oplaty") : "Expected URL to contain 'poryadok-oplaty', but got: " + newUrl;
        try {
            wait.until(ExpectedConditions.visibilityOf($("body")));
            String bodyText = $("body").getText();
            assert !bodyText.contains("404") && !bodyText.contains("Not Found") && !bodyText.contains("Ошибка")
                    : "Page returned error: " + bodyText;
        } catch (Exception e) {
            throw new AssertionError("Page content verification failed", e);
        }
        back();
        wait.until(ExpectedConditions.urlToBe(currentUrl));
        return this;
    }

    public HomePage validateCurrentUrl() {
        String url = WebDriverRunner.url();
        assert url != null && !url.isEmpty() : "URL is empty";
        return this;
    }

    public HomePage validateCurrentUrl(String expectedPart) {
        String url = WebDriverRunner.url();
        assert url != null && url.contains(expectedPart) :
                "URL does not contain '" + expectedPart + "'. Actual URL: " + url;
        return this;
    }

    public HomePage selectServicesTab() {
        closeCookieBanner();
        selectHeader.shouldBe(visible).click();
        selectOption.shouldBe(visible).click();
        connectionForm.shouldBe(visible);
        return this;
    }

    public HomePage fillPhoneNumber(String phone) {
        phoneInput.shouldBe(visible).clear();
        phoneInput.setValue(phone.replaceAll("\\D", ""));
        return this;
    }

    public HomePage fillAmount(String amount) {
        amountInput.shouldBe(visible).clear();
        amountInput.setValue(amount);
        return this;
    }

    public HomePage fillEmail(String email) {
        emailInput.shouldBe(visible).clear();
        emailInput.setValue(email);
        return this;
    }

    public PaymentPage clickContinue() {
        continueButton.shouldBe(visible).click();
        return new PaymentPage();
    }
}