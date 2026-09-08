package pages;

import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public class PaymentFormPage {

    private final SelenideElement cardNumberInput = $("#cc-number");
    private final SelenideElement payButton = $("button.button_colored, button[class*='colored'], button[class*='pay']");

    public PaymentFormPage verifyPaymentFormOpened() {
        String currentUrl = WebDriverRunner.url();
        System.out.println("   Текущий URL после нажатия 'Продолжить': " + currentUrl);

        WebDriverWait wait = new WebDriverWait(
                WebDriverRunner.getWebDriver(),
                Duration.ofSeconds(15)
        );

        System.out.println("   🔍 Ожидание появления iframe...");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                org.openqa.selenium.By.cssSelector("iframe[src*='bepaid'], iframe[src*='pay'], iframe[src*='payment']")
        ));

        SelenideElement iframe = $("iframe[src*='bepaid'], iframe[src*='pay'], iframe[src*='payment']");
        System.out.println("   🔍 Найден iframe: " + iframe.getAttribute("src"));

        switchTo().frame(iframe);
        System.out.println("   ✅ Переключились на iframe");

        wait.until(driver -> {
            SelenideElement el = $("#cc-number");
            return el.isDisplayed();
        });
        System.out.println("   ✅ Поле 'Номер карты' найдено (id: cc-number)");

        try {
            payButton.shouldBe(visible, Duration.ofSeconds(5));
            System.out.println("   ✅ Кнопка 'Оплатить' найдена");
        } catch (Exception e) {
            System.out.println("   ⚠️ Кнопка 'Оплатить' не найдена");
        }

        System.out.println("   ✅ Проверка формы оплаты завершена");
        return this;
    }
}