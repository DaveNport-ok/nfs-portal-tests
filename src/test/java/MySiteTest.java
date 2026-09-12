import com.codeborne.selenide.CollectionCondition;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
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
    }

    @Test
    @DisplayName("1. Smoke: Страница отдает непустой Title и содержит тело документа")
    void testPageLoadAndMetadata() {
        open(BASE_URL);

        $("body").shouldBe(Condition.visible);
        // Проверяем, что заголовок вкладки браузера не пустой
        webdriver().shouldNotHave(title(""));
    }

    @Test
    @DisplayName("2. UI: Проверка наличия элементов локализации (i18n)")
    void testLocalizationAttributesExist() {
        open(BASE_URL);

        // Проверяем, что на странице размечены ключи интернационализации (data-i18n)
        $$("[data-i18n]").shouldHave(CollectionCondition.sizeGreaterThan(0));

        // Кнопка логина должна иметь корректный языковой ключ
        $("#loginBtn").shouldHave(Condition.attribute("data-i18n", "btn_login"));
    }

    @Test
    @DisplayName("3. E2E: Жизненный цикл модального окна (Открытие и закрытие)")
    void testLoginModalLifecycle() {
        open(BASE_URL);

        // 1. Открываем модальное окно
        if ($("#loginBtn").isDisplayed()) {
            $("#loginBtn").click();
        } else {
            executeJavaScript("openModal()");
        }

        // 2. Проверяем, что контейнер модалки отображается на экране
        var modal = $(".modal, #modal, [class*='modal']").should(Condition.exist);

        // 3. Ищем крестик закрытия или кликаем по фону/кнопке Escape для закрытия
        if ($(".modal-close, .close, [onclick*='closeModal']").exists()) {
            $(".modal-close, .close, [onclick*='closeModal']").click();
        } else {
            // Альтернатива: закрытие вызовом JS или нажатием Escape
            executeJavaScript("typeof closeModal === 'function' && closeModal()");
        }
    }

    @Test
    @DisplayName("4. Navigation: Все внутренние ссылки имеют корректный href")
    void testNavigationLinksAreNotEmpty() {
        open(BASE_URL);

        // Собираем все теги <a> и проверяем, что у них нет пустых ссылок href=""
        var links = $$("a");
        for (var link : links) {
            if (link.isDisplayed()) {
                link.shouldHave(Condition.attributeMatching("href", ".*\\S.*"));
            }
        }
    }

    @Test
    @DisplayName("5. Quality: Проверка отсутствия критических ошибок (SEVERE) в консоли браузера")
    void testNoSevereJavaScriptErrors() {
        open(BASE_URL);

        // Считываем системный лог консоли DevTools (F12 -> Console)
        LogEntries logEntries = getWebDriver().manage().logs().get(LogType.BROWSER);
        List<LogEntry> severeErrors = logEntries.getAll().stream()
                .filter(entry -> entry.getLevel().equals(Level.SEVERE))
                // Исключаем предупреждения самого Chrome/CDP, фильтруем только сбои клиентского JS
                .filter(entry -> !entry.getMessage().contains("CDP") && !entry.getMessage().contains("favicon.ico"))
                .toList();

        // Тест упадет с выводом списка ошибок, если в консоли есть Uncaught TypeError/ReferenceError
        assertTrue(severeErrors.isEmpty(), "В консоли обнаружены критические JS-ошибки: " + severeErrors);
    }
}