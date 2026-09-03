package pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class HomePage {

    // === ЛОКАТОРЫ ===
    private final SelenideElement blockTitle = $("div.pay__wrapper h2");
    private final SelenideElement selectHeader = $(".select__header");
    private final SelenideElement connectionForm = $("#pay-connection");
    private final SelenideElement internetForm = $("#pay-internet");
    private final SelenideElement instalmentForm = $("#pay-instalment");
    private final SelenideElement arrearsForm = $("#pay-arrears");

    // Поля для ввода (динамические, зависят от выбранной формы)
    private SelenideElement getPhoneInput() {
        return $(".pay-form.opened .phone, .pay-form.opened input[placeholder*='Номер']");
    }

    private SelenideElement getAmountInput() {
        return $(".pay-form.opened .total_rub, .pay-form.opened input[placeholder='Сумма']");
    }

    private SelenideElement getEmailInput() {
        return $(".pay-form.opened .email, .pay-form.opened input[placeholder*='E-mail']");
    }

    // Кнопка "Продолжить" для текущей формы
    private SelenideElement getContinueButton() {
        return $(".pay-form.opened .button__default");
    }

    // Логотипы и ссылки
    private final ElementsCollection paymentLogos = $$("div.pay__partners img");
    private final SelenideElement moreDetailsLink = $("div.pay__wrapper a[href*='poryadok-oplaty']");

    // Cookie
    private final SelenideElement cookieBanner = $(".cookie.show");
    private final SelenideElement cookieAcceptButton = $(".cookie__ok");

    // === МЕТОДЫ ===

    public HomePage openPage() {
        open("https://www.mts.by");
        return this;
    }

    private HomePage closeCookieBanner() {
        if (cookieBanner.isDisplayed()) {
            try {
                cookieAcceptButton.click();
                Thread.sleep(1000);
            } catch (Exception e) {
                // ignore
            }
        }
        return this;
    }

    public HomePage verifyBlockTitle() {
        blockTitle.shouldBe(visible);
        blockTitle.shouldHave(text("Онлайн пополнение"));
        return this;
    }

    public HomePage verifyPaymentLogosExist() {
        paymentLogos.shouldHave(sizeGreaterThan(0));
        paymentLogos.forEach(logo -> logo.shouldBe(visible));
        return this;
    }

    public HomePage clickMoreDetailsAndVerify() {
        moreDetailsLink.shouldBe(visible).click();
        String currentUrl = WebDriverRunner.url();
        back();
        return this;
    }

    public HomePage selectPaymentOption(String optionName) {
        closeCookieBanner();

        selectHeader.shouldBe(visible).click();

        SelenideElement option = $x(String.format("//li[contains(@class, 'select__item')]//p[text()='%s']", optionName));
        option.shouldBe(visible).click();

        // Ждем, пока откроется нужная форма
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return this;
    }

    public HomePage verifyPlaceholders(String expectedPhonePlaceholder, String expectedAmountPlaceholder, String expectedEmailPlaceholder) {
        // Ждем, пока форма загрузится
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Проверяем плейсхолдеры у активной формы
        SelenideElement phoneInput = getPhoneInput();
        SelenideElement amountInput = getAmountInput();
        SelenideElement emailInput = getEmailInput();

        phoneInput.shouldHave(attribute("placeholder", expectedPhonePlaceholder));
        amountInput.shouldHave(attribute("placeholder", expectedAmountPlaceholder));
        emailInput.shouldHave(attribute("placeholder", expectedEmailPlaceholder));

        return this;
    }

    public HomePage fillPhoneNumber(String phone) {
        SelenideElement phoneInput = getPhoneInput();
        phoneInput.shouldBe(visible).clear();
        phoneInput.setValue(phone.replaceAll("\\D", ""));
        return this;
    }

    public HomePage fillAmount(String amount) {
        SelenideElement amountInput = getAmountInput();
        amountInput.shouldBe(visible).clear();
        amountInput.setValue(amount);
        return this;
    }

    public HomePage fillEmail(String email) {
        SelenideElement emailInput = getEmailInput();
        emailInput.shouldBe(visible).clear();
        emailInput.setValue(email);
        return this;
    }

    public PaymentFormPage clickContinue() {
        SelenideElement continueButton = getContinueButton();
        continueButton.shouldBe(visible).click();
        return new PaymentFormPage();
    }
}