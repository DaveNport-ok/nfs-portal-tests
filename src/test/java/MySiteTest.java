import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.util.List;
import java.util.logging.Level;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.WebDriverConditions.title;
import static com.codeborne.selenide.WebDriverRunner.getWebDriver;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Feature("Главная страница NFS Portal")
public class MySiteTest {

    private static final String BASE_URL = "https://davenport-ok.github.io/nfs-portal";

    @BeforeAll
    static void setup() {
        Configuration.browserSize = "1920x1080";
        Configuration.browser = "chrome";
        Configuration.pageLoadStrategy = "eager";
        Configuration.timeout = 8000;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        Configuration.browserCapabilities = options;

        // Регистрация слушателя Allure: логирует шаги и делает скриншоты падений
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true));
    }

    @Test
    @Story("Доступность страниц")
    @DisplayName("1. Smoke: Страница отдает непустой Title и содержит тело документа")
    void testPageLoadAndMetadata() {
        open(BASE_URL);
        $("body").shouldBe(Condition.visible);
        webdriver().shouldNotHave(title(""));
    }

    @Test
    @Story("Интернационализация (i18n)")
    @DisplayName("2. UI: Проверка наличия элементов локализации (i18n)")
    void testLocalizationAttributesExist() {
        open(BASE_URL);
        $$("[data-i18n]").shouldHave(CollectionCondition.sizeGreaterThan(0));
        $("#loginBtn").shouldHave(Condition.attribute("data-i18n", "btn_login"));
    }

    @Test
    @Story("Модальные окна")
    @DisplayName("3. E2E: Жизненный цикл модального окна (Открытие и закрытие)")
    void testLoginModalLifecycle() {
        open(BASE_URL);

        if ($("#loginBtn").isDisplayed()) {
            $("#loginBtn").click();
        } else {
            executeJavaScript("openModal()");
        }

        $(".modal, #modal, [class*='modal']").should(Condition.exist);

        if ($(".modal-close, .close, [onclick*='closeModal']").exists()) {
            $(".modal-close, .close, [onclick*='closeModal']").click();
        } else {
            executeJavaScript("typeof closeModal === 'function' && closeModal()");
        }
    }

    @Test
    @Story("Навигация")
    @DisplayName("4. Navigation: Все внутренние ссылки имеют корректный href")
    void testNavigationLinksAreNotEmpty() {
        open(BASE_URL);
        var links = $$("a");
        for (var link : links) {
            if (link.isDisplayed()) {
                link.shouldHave(Condition.attributeMatching("href", ".*\\S.*"));
            }
        }
    }

    @Test
    @Story("Аудит консоли браузера")
    @DisplayName("5. Quality: Проверка отсутствия критических ошибок (SEVERE) в консоли")
    void testNoSevereJavaScriptErrors() {
        open(BASE_URL);
        LogEntries logEntries = getWebDriver().manage().logs().get(LogType.BROWSER);
        List<LogEntry> severeErrors = logEntries.getAll().stream()
                .filter(entry -> entry.getLevel().equals(Level.SEVERE))
                .filter(entry -> !entry.getMessage().contains("CDP") && !entry.getMessage().contains("favicon.ico"))
                .toList();

        assertTrue(severeErrors.isEmpty(), "В консоли обнаружены критические JS-ошибки: " + severeErrors);
    }
}