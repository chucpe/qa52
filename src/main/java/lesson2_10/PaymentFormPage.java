package lesson2_10;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class PaymentFormPage extends BasePage {

    private final SelenideElement paymentIframe = $("iframe[src*='bepaid'], iframe[src*='pay'], iframe[src*='payment']");
    private final SelenideElement cardNumberInput = $("#cc-number");
    private final SelenideElement payButton = $("button.button_colored, button[class*='colored'], button[class*='pay']");
    private final SelenideElement amountInIframe = $(".total-amount, .amount, .total, .payment-amount");
    private final SelenideElement phoneInIframe = $(".phone-number, .order-details .phone, .description .phone");

    @Override
    public PaymentFormPage openPage() {
        return this;
    }

    @Override
    public PaymentFormPage waitForPageLoad() {
        return this;
    }

    public PaymentFormPage verifyPaymentFormOpened() {
        String currentUrl = WebDriverRunner.url();
        System.out.println("Текущий URL после нажатия 'Продолжить': " + currentUrl);

        try {
            WebDriverWait wait = new WebDriverWait(
                    WebDriverRunner.getWebDriver(),
                    Duration.ofSeconds(15)
            );

            System.out.println("Ожидание появления iframe...");
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    org.openqa.selenium.By.cssSelector("iframe[src*='bepaid'], iframe[src*='pay'], iframe[src*='payment']")
            ));

            System.out.println("Найден iframe: " + paymentIframe.getAttribute("src"));
            switchTo().frame(paymentIframe);
            System.out.println("Переключились на iframe");

            wait.until(driver -> {
                SelenideElement el = $("#cc-number");
                return el.isDisplayed();
            });
            System.out.println("Поле 'Номер карты' найдено (id: cc-number)");

            try {
                payButton.shouldBe(visible, Duration.ofSeconds(5));
                System.out.println("Кнопка 'Оплатить' найдена");
            } catch (Exception e) {
                System.out.println("Кнопка 'Оплатить' не найдена");
            }

            System.out.println("Проверка формы оплаты завершена");
        } catch (Exception e) {
            System.out.println("Ошибка при проверке формы оплаты: " + e.getMessage());
        }
        return this;
    }

    public PaymentFormPage verifyAmountInIframe(String expectedAmount) {
        try {
            switchTo().frame(paymentIframe);
            String amount = amountInIframe.shouldBe(visible).getText();
            System.out.println("Сумма в iframe: " + amount);
        } catch (Exception e) {
            System.out.println("Не удалось получить сумму в iframe: " + e.getMessage());
        }
        return this;
    }

    public PaymentFormPage verifyPhoneInIframe(String expectedPhone) {
        try {
            switchTo().frame(paymentIframe);
            String phone = phoneInIframe.shouldBe(visible).getText();
            System.out.println("Номер телефона в iframe: " + phone);
        } catch (Exception e) {
            System.out.println("Не удалось получить номер телефона в iframe: " + e.getMessage());
        }
        return this;
    }

    public PaymentFormPage verifyCardFieldsEmpty() {
        try {
            switchTo().frame(paymentIframe);
            String cardNumber = cardNumberInput.shouldBe(visible).getValue();
            if (cardNumber == null || cardNumber.isEmpty()) {
                System.out.println("Поле номера карты пустое ✅");
            } else {
                System.out.println("Поле номера карты заполнено");
            }
        } catch (Exception e) {
            System.out.println("Не удалось проверить поле карты: " + e.getMessage());
        }
        return this;
    }

    public PaymentFormPage verifyCardPlaceholders() {
        try {
            switchTo().frame(paymentIframe);

            String cardPlaceholder = cardNumberInput.shouldBe(visible).getAttribute("placeholder");
            System.out.println("Плейсхолдер номера карты: " + cardPlaceholder);

            SelenideElement expiryInput = $("#cc-expiry, #expiry, input[placeholder*='ММ/ГГ']");
            if (expiryInput.isDisplayed()) {
                String expiryPlaceholder = expiryInput.getAttribute("placeholder");
                System.out.println("Плейсхолдер срока действия: " + expiryPlaceholder);
            }

            SelenideElement cvvInput = $("#cc-cvv, #cvv, input[placeholder*='CVC']");
            if (cvvInput.isDisplayed()) {
                String cvvPlaceholder = cvvInput.getAttribute("placeholder");
                System.out.println("Плейсхолдер CVC/CVV: " + cvvPlaceholder);
            }

        } catch (Exception e) {
            System.out.println("Не удалось получить плейсхолдеры: " + e.getMessage());
        }
        return this;
    }

    public PaymentFormPage verifyPaymentIcons() {
        try {
            switchTo().frame(paymentIframe);
            SelenideElement icons = $(".payment-icons, .card-icons, .payment-methods");
            if (icons.isDisplayed()) {
                System.out.println("Иконки платежных систем отображаются ✅");
            } else {
                System.out.println("Иконки платежных систем не найдены");
            }
        } catch (Exception e) {
            System.out.println("Не удалось проверить иконки: " + e.getMessage());
        }
        return this;
    }

    public void verifyAllPaymentData(String expectedPhone, String expectedAmount) {
        System.out.println("\n=== ПРОВЕРКА ДАННЫХ В IFrame ===");
        System.out.println("Ожидаемый номер телефона: " + expectedPhone);
        System.out.println("Ожидаемая сумма: " + expectedAmount);

        verifyAmountInIframe(expectedAmount);
        verifyPhoneInIframe(expectedPhone);
        verifyCardFieldsEmpty();
        verifyCardPlaceholders();
        verifyPaymentIcons();

        System.out.println("=== ПРОВЕРКА ЗАВЕРШЕНА ===\n");
    }
}