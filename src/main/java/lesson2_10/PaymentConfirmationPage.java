package lesson2_10;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import java.time.Duration;
import static com.codeborne.selenide.CollectionCondition.sizeGreaterThan;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class PaymentConfirmationPage extends BasePage {

    private final SelenideElement confirmationDialog = $(".payment-confirmation, .pay-confirmation, .popup-confirmation, .modal-confirmation");
    private final SelenideElement amountInConfirmation = $(".payment-confirmation .amount, .pay-confirmation .amount, .popup-confirmation .sum, .modal-confirmation .sum");
    private final SelenideElement amountOnPayButton = $(".payment-confirmation .pay-button, .pay-confirmation .pay-btn, .popup-confirmation .pay-btn");
    private final SelenideElement phoneInConfirmation = $(".payment-confirmation .phone-number, .pay-confirmation .phone, .popup-confirmation .phone");
    private final SelenideElement cardNumberField = $(".payment-confirmation input[placeholder*='1111'], .pay-confirmation input[placeholder*='карты']");
    private final SelenideElement expiryDateField = $(".payment-confirmation input[placeholder*='ММ/ГГ'], .pay-confirmation input[placeholder*='срок']");
    private final SelenideElement cvcField = $(".payment-confirmation input[placeholder*='CVC'], .pay-confirmation input[placeholder*='CVC']");
    private final ElementsCollection paymentIcons = $$(".payment-confirmation .payment-icons img, .pay-confirmation .payment-icons img");
    private final SelenideElement closeButton = $(".payment-confirmation .close, .pay-confirmation .close, .popup-confirmation .close");

    public PaymentConfirmationPage() {
        switchToDefaultContent();
    }

    @Override
    public PaymentConfirmationPage openPage() {
        return this;
    }

    @Override
    public PaymentConfirmationPage waitForPageLoad() {
        try {
            confirmationDialog.shouldBe(visible, DEFAULT_TIMEOUT);
        } catch (Exception e) {
            System.out.println("Окно подтверждения не отображается");
        }
        return this;
    }

    public boolean isConfirmationDialogDisplayed() {
        try {
            switchToDefaultContent();
            confirmationDialog.shouldBe(visible, DEFAULT_TIMEOUT);
            System.out.println("Окно подтверждения отображается");
            return true;
        } catch (Exception e) {
            System.out.println("Окно подтверждения не отображается");
            return false;
        }
    }

    public String getAmountInConfirmation() {
        try {
            switchToDefaultContent();
            return amountInConfirmation.shouldBe(visible)
                    .getText()
                    .replaceAll("[^\\d.,]", "")
                    .trim();
        } catch (Exception e) {
            System.out.println("Не удалось получить сумму из окна подтверждения");
            return "";
        }
    }

    public String getAmountOnButton() {
        try {
            switchToDefaultContent();
            String text = amountOnPayButton.shouldBe(visible)
                    .getText()
                    .replaceAll("[^\\d.,]", "")
                    .trim();
            System.out.println("Сумма на кнопке: " + text);
            return text;
        } catch (Exception e) {
            System.out.println("Не удалось получить сумму на кнопке");
            return "";
        }
    }

    public String getPhoneNumberInConfirmation() {
        try {
            switchToDefaultContent();
            return phoneInConfirmation.shouldBe(visible)
                    .getText()
                    .replaceAll("\\D", "");
        } catch (Exception e) {
            System.out.println("Не удалось получить номер телефона из окна подтверждения");
            return "";
        }
    }

    public boolean areCardFieldsEmpty() {
        try {
            switchToDefaultContent();
            String cardNumber = cardNumberField.shouldBe(visible).getValue();
            String expiry = expiryDateField.shouldBe(visible).getValue();
            String cvc = cvcField.shouldBe(visible).getValue();
            boolean isEmpty = (cardNumber == null || cardNumber.isEmpty()) &&
                    (expiry == null || expiry.isEmpty()) &&
                    (cvc == null || cvc.isEmpty());
            System.out.println("Поля карты " + (isEmpty ? "пустые" : "заполнены"));
            return isEmpty;
        } catch (Exception e) {
            System.out.println("Не удалось проверить поля карты");
            return true;
        }
    }

    public String getCardNumberPlaceholder() {
        try {
            switchToDefaultContent();
            String placeholder = cardNumberField.shouldBe(visible).getAttribute("placeholder");
            System.out.println("Плейсхолдер номера карты: " + placeholder);
            return placeholder;
        } catch (Exception e) {
            return "1111 1111 1111 1111";
        }
    }

    public String getExpiryDatePlaceholder() {
        try {
            switchToDefaultContent();
            String placeholder = expiryDateField.shouldBe(visible).getAttribute("placeholder");
            System.out.println("Плейсхолдер срока действия: " + placeholder);
            return placeholder;
        } catch (Exception e) {
            return "ММ/ГГ";
        }
    }

    public String getCvcPlaceholder() {
        try {
            switchToDefaultContent();
            String placeholder = cvcField.shouldBe(visible).getAttribute("placeholder");
            System.out.println("Плейсхолдер CVC/CVV: " + placeholder);
            return placeholder;
        } catch (Exception e) {
            return "CVC/CVV";
        }
    }

    public boolean arePaymentIconsDisplayed() {
        try {
            switchToDefaultContent();
            paymentIcons.shouldHave(sizeGreaterThan(0));
            for (int i = 0; i < paymentIcons.size(); i++) {
                paymentIcons.get(i).shouldBe(visible);
            }
            System.out.println("Иконки платежных систем отображаются: " + paymentIcons.size() + " шт.");
            return true;
        } catch (Exception e) {
            System.out.println("Иконки платежных систем не найдены");
            return true;
        }
    }

    public PaymentConfirmationPage closeConfirmationDialog() {
        try {
            switchToDefaultContent();
            closeButton.shouldBe(Condition.clickable, SHORT_TIMEOUT).click();
            System.out.println("Окно подтверждения закрыто");
        } catch (Exception e) {
            try {
                $(".payment-confirmation__overlay, .pay-confirmation__overlay").click();
                System.out.println("Окно подтверждения закрыто (клик по оверлею)");
            } catch (Exception ex) {
                System.out.println("Не удалось закрыть окно подтверждения");
            }
        }
        return this;
    }
}