package pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Step;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class HomePage {

    // === ЛОКАТОРЫ ===
    private final SelenideElement blockTitle = $("div.pay__wrapper h2");
    private final SelenideElement selectHeader = $(".select__header");
    private final ElementsCollection paymentLogos = $$("div.pay__partners img");
    private final SelenideElement moreDetailsLink = $("div.pay__wrapper a[href*='poryadok-oplaty']");
    private final SelenideElement cookieBanner = $(".cookie.show");
    private final SelenideElement cookieAcceptButton = $(".cookie__ok");

    // Динамические поля
    private SelenideElement getPhoneInput() {
        return $(".pay-form.opened .phone, .pay-form.opened input[placeholder*='Номер']");
    }

    private SelenideElement getAmountInput() {
        return $(".pay-form.opened .total_rub, .pay-form.opened input[placeholder='Сумма']");
    }

    private SelenideElement getEmailInput() {
        return $(".pay-form.opened .email, .pay-form.opened input[placeholder*='E-mail']");
    }

    private SelenideElement getContinueButton() {
        return $(".pay-form.opened .button__default");
    }

    // === МЕТОДЫ С ALLURE ШАГАМИ ===

    @Step("Открыть главную страницу mts.by")
    public HomePage openPage() {
        open("https://www.mts.by");
        sleep(1000);
        return this;
    }

    @Step("Закрыть cookie-баннер")
    private HomePage closeCookieBanner() {
        if (cookieBanner.isDisplayed()) {
            try {
                cookieAcceptButton.click();
                sleep(500);
            } catch (Exception e) {
                // ignore
            }
        }
        return this;
    }

    @Step("Проверить название блока 'Онлайн пополнение без комиссии'")
    public HomePage verifyBlockTitle() {
        blockTitle.shouldBe(visible);
        blockTitle.shouldHave(text("Онлайн пополнение"));
        return this;
    }

    @Step("Проверить наличие логотипов платёжных систем")
    public HomePage verifyPaymentLogosExist() {
        paymentLogos.shouldHave(sizeGreaterThan(0));
        paymentLogos.forEach(logo -> logo.shouldBe(visible));
        return this;
    }

    @Step("Проверить работу ссылки «Подробнее о сервисе»")
    public HomePage clickMoreDetailsAndVerify() {
        moreDetailsLink.shouldBe(visible).click();
        String currentUrl = WebDriverRunner.url();
        back();
        return this;
    }

    @Step("Выбрать вариант оплаты: {optionName}")
    public HomePage selectPaymentOption(String optionName) {
        closeCookieBanner();

        // Закрываем список, если он уже открыт
        SelenideElement dropdownList = $(".select__list");
        if (dropdownList.exists() && dropdownList.isDisplayed()) {
            selectHeader.click();
            sleep(300);
        }

        // Открываем список
        selectHeader.shouldBe(visible).click();
        sleep(300);

        // Находим нужную опцию
        SelenideElement option = $x(String.format("//li[contains(@class, 'select__item')]//p[text()='%s']", optionName));
        option.shouldBe(visible);

        // Прокручиваем к элементу и кликаем через JavaScript (обходит перекрытие)
        executeJavaScript("arguments[0].scrollIntoView({block: 'center'});", option);
        sleep(200);
        executeJavaScript("arguments[0].click();", option);

        sleep(500);
        return this;
    }

    @Step("Проверить плейсхолдеры: телефон='{phonePlaceholder}', сумма='{amountPlaceholder}', email='{emailPlaceholder}'")
    public HomePage verifyPlaceholders(String phonePlaceholder, String amountPlaceholder, String emailPlaceholder) {
        sleep(500);

        getPhoneInput().shouldHave(attribute("placeholder", phonePlaceholder));
        getAmountInput().shouldHave(attribute("placeholder", amountPlaceholder));
        getEmailInput().shouldHave(attribute("placeholder", emailPlaceholder));

        return this;
    }

    @Step("Заполнить номер телефона: {phone}")
    public HomePage fillPhoneNumber(String phone) {
        SelenideElement phoneInput = getPhoneInput();
        phoneInput.shouldBe(visible).clear();
        phoneInput.setValue(phone.replaceAll("\\D", ""));
        return this;
    }

    @Step("Заполнить сумму: {amount}")
    public HomePage fillAmount(String amount) {
        SelenideElement amountInput = getAmountInput();
        amountInput.shouldBe(visible).clear();
        amountInput.setValue(amount);
        return this;
    }

    @Step("Заполнить email: {email}")
    public HomePage fillEmail(String email) {
        SelenideElement emailInput = getEmailInput();
        emailInput.shouldBe(visible).clear();
        emailInput.setValue(email);
        return this;
    }

    @Step("Нажать кнопку «Продолжить»")
    public PaymentFormPage clickContinue() {
        SelenideElement continueButton = getContinueButton();
        continueButton.shouldBe(visible).click();
        return new PaymentFormPage();
    }
}