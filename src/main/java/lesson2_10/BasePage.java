package lesson2_10;

import com.codeborne.selenide.SelenideElement;
import java.time.Duration;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;

public abstract class BasePage {

    protected final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    protected final Duration SHORT_TIMEOUT = Duration.ofSeconds(5);

    public abstract BasePage openPage();

    public abstract BasePage waitForPageLoad();

    protected void closeCookieBanner() {
        try {
            SelenideElement cookieBanner = $(".cookie.show, .cookie, .cookie-banner, .cookie-consent");
            if (cookieBanner.isDisplayed()) {
                SelenideElement acceptButton = $(".cookie__ok, .cookie-accept, .accept-cookies, .cookie-consent__button");
                if (acceptButton.isDisplayed()) {
                    acceptButton.click();
                }
            }
        } catch (Exception e) {
        }
    }

    protected void switchToDefaultContent() {
        try {
            switchTo().defaultContent();
        } catch (Exception e) {
            // ignore
        }
    }
}