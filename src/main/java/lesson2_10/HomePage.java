package lesson2_10;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import java.time.Duration;

import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class HomePage extends BasePage {

    // Локаторы главной страницы
    private final SelenideElement blockTitle = $("div.pay__wrapper h2");
    private final SelenideElement selectHeader = $(".select__header");
    private final ElementsCollection paymentLogos = $$("div.pay__partners img");
    private final SelenideElement moreDetailsLink = $("div.pay__wrapper a[href*='poryadok-oplaty']");
    private final SelenideElement continueButton = $(".pay-form.opened .button__default");

    // Данные для разных вариантов оплаты
    private static final String PAYMENT_OPTION_SERVICES = "Услуги связи";
    private static final String PAYMENT_OPTION_INTERNET = "Домашний интернет";
    private static final String PAYMENT_OPTION_INSTALMENT = "Рассрочка";
    private static final String PAYMENT_OPTION_ARREARS = "Задолженность";

    // Ожидаемые плейсхолдеры для каждого варианта
    public static final String[] PLACEHOLDERS_SERVICES = {"Номер телефона", "Сумма", "E-mail для отправки чека"};
    public static final String[] PLACEHOLDERS_INTERNET = {"Номер абонента", "Сумма", "E-mail для отправки чека"};
    public static final String[] PLACEHOLDERS_INSTALMENT = {"Номер счета на 44", "Сумма", "E-mail для отправки чека"};
    public static final String[] PLACEHOLDERS_ARREARS = {"Номер счета на 2073", "Сумма", "E-mail для отправки чека"};

    @Override
    public HomePage openPage() {
        open("https://www.mts.by");
        return this;
    }

    @Override
    public HomePage waitForPageLoad() {
        blockTitle.shouldBe(visible, DEFAULT_TIMEOUT);
        return this;
    }

    // ===================== МЕТОДЫ ПРОВЕРОК =====================

    /**
     * Проверка заголовка блока
     */
    public HomePage verifyBlockTitle() {
        blockTitle.shouldBe(visible);
        blockTitle.shouldHave(text("Онлайн пополнение"));
        return this;
    }

    /**
     * Проверка наличия логотипов платежных систем
     */
    public HomePage verifyPaymentLogosExist() {
        paymentLogos.shouldHave(sizeGreaterThan(0));
        paymentLogos.forEach(logo -> logo.shouldBe(visible));
        return this;
    }

    /**
     * Клик по ссылке "Подробнее"
     */
    public HomePage clickMoreDetailsAndVerify() {
        moreDetailsLink.shouldBe(visible).click();
        // Проверка открытия страницы с подробностями
        $("div.details-content").shouldBe(visible, DEFAULT_TIMEOUT);
        back();
        return this;
    }

    // ===================== МЕТОДЫ РАБОТЫ С ФОРМОЙ =====================

    /**
     * Выбор варианта оплаты
     */
    public HomePage selectPaymentOption(String optionName) {
        closeCookieBanner();

        selectHeader.shouldBe(visible).click();

        SelenideElement option = $x(String.format("//li[contains(@class, 'select__item')]//p[text()='%s']", optionName));
        option.shouldBe(visible).click();

        sleep(1000); // Ожидание загрузки формы

        return this;
    }

    /**
     * Получение полей ввода для текущей активной формы
     */
    private SelenideElement getPhoneInput() {
        return $(".pay-form.opened .phone, .pay-form.opened input[placeholder*='Номер']");
    }

    private SelenideElement getAmountInput() {
        return $(".pay-form.opened .total_rub, .pay-form.opened input[placeholder='Сумма']");
    }

    private SelenideElement getEmailInput() {
        return $(".pay-form.opened .email, .pay-form.opened input[placeholder*='E-mail']");
    }

    /**
     * Проверка плейсхолдеров для текущего варианта оплаты
     */
    public HomePage verifyPlaceholders(String expectedPhonePlaceholder,
                                       String expectedAmountPlaceholder,
                                       String expectedEmailPlaceholder) {
        sleep(500);

        getPhoneInput().shouldHave(attribute("placeholder", expectedPhonePlaceholder));
        getAmountInput().shouldHave(attribute("placeholder", expectedAmountPlaceholder));
        getEmailInput().shouldHave(attribute("placeholder", expectedEmailPlaceholder));

        return this;
    }

    /**
     * Проверка плейсхолдеров с использованием массива
     */
    public HomePage verifyPlaceholders(String[] expectedPlaceholders) {
        if (expectedPlaceholders.length >= 3) {
            verifyPlaceholders(
                    expectedPlaceholders[0],
                    expectedPlaceholders[1],
                    expectedPlaceholders[2]
            );
        }
        return this;
    }

    /**
     * Проверка плейсхолдеров для всех вариантов оплаты
     */
    public HomePage verifyAllPaymentOptionsPlaceholders() {
        // Услуги связи
        selectPaymentOption(PAYMENT_OPTION_SERVICES);
        verifyPlaceholders(PLACEHOLDERS_SERVICES);

        // Домашний интернет
        selectPaymentOption(PAYMENT_OPTION_INTERNET);
        verifyPlaceholders(PLACEHOLDERS_INTERNET);

        // Рассрочка
        selectPaymentOption(PAYMENT_OPTION_INSTALMENT);
        verifyPlaceholders(PLACEHOLDERS_INSTALMENT);

        // Задолженность
        selectPaymentOption(PAYMENT_OPTION_ARREARS);
        verifyPlaceholders(PLACEHOLDERS_ARREARS);

        return this;
    }

    // ===================== МЕТОДЫ ЗАПОЛНЕНИЯ ФОРМЫ =====================

    /**
     * Заполнение номера телефона
     */
    public HomePage fillPhoneNumber(String phone) {
        SelenideElement phoneInput = getPhoneInput();
        phoneInput.shouldBe(visible).clear();
        phoneInput.setValue(phone.replaceAll("\\D", ""));
        return this;
    }

    /**
     * Заполнение суммы
     */
    public HomePage fillAmount(String amount) {
        SelenideElement amountInput = getAmountInput();
        amountInput.shouldBe(visible).clear();
        amountInput.setValue(amount);
        return this;
    }

    /**
     * Заполнение email
     */
    public HomePage fillEmail(String email) {
        SelenideElement emailInput = getEmailInput();
        emailInput.shouldBe(visible).clear();
        emailInput.setValue(email);
        return this;
    }

    // ===================== ПЕРЕХОД НА СЛЕДУЮЩУЮ СТРАНИЦУ =====================

    /**
     * Клик по кнопке "Продолжить"
     */
    public PaymentFormPage clickContinue() {
        continueButton.shouldBe(visible).click();
        return new PaymentFormPage();
    }
}
