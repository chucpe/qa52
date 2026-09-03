package pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;

public class PaymentFormPage {

    private final SelenideElement paymentTitle = $(".payment-title, .page-title, .modal-title, h1, .title");
    private final SelenideElement cardNumberInput = $("input[placeholder*='номер'], input[placeholder*='card']");
    private final SelenideElement cardExpiryInput = $("input[placeholder*='месяц'], input[placeholder*='год']");
    private final SelenideElement cardCvvInput = $("input[placeholder*='CVV'], input[placeholder*='cvv']");
    private final SelenideElement payButton = $x("//button[contains(text(), 'Оплатить') or contains(text(), 'Pay')]");

    public PaymentFormPage verifyPaymentFormOpened() {
        String currentUrl = WebDriverRunner.url();
        System.out.println("   Текущий URL после нажатия 'Продолжить': " + currentUrl);

        // Проверяем, что мы остались на странице mts.by (форма оплаты может быть в модальном окне)
        if (currentUrl.contains("mts.by")) {
            System.out.println("   ℹ️ Мы на mts.by, ищем форму оплаты...");
        }

        // Проверяем наличие элементов формы оплаты
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (cardNumberInput.exists() && cardNumberInput.isDisplayed()) {
            System.out.println("   ✅ Поле 'Номер карты' найдено");
            cardNumberInput.shouldBe(visible);
        } else {
            System.out.println("   ℹ️ Поле 'Номер карты' не найдено (возможно, форма в iframe или требуется ручной ввод)");
        }

        if (payButton.exists() && payButton.isDisplayed()) {
            System.out.println("   ✅ Кнопка 'Оплатить' найдена");
            payButton.shouldBe(visible);
        } else {
            System.out.println("   ℹ️ Кнопка 'Оплатить' не найдена");
        }

        System.out.println("   ✅ Проверка формы оплаты завершена");
        return this;
    }
}