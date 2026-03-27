package ru.lessons.my.e2e;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import ru.lessons.my.BaseE2ETest;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VehiclesE2ETest extends BaseE2ETest {

    @BeforeEach
    public void init() {
        login();
    }

    @Test
    @Order(1)
    public void displayVehiclesTest() {
        page.click("a[href*='/vehicles?enterpriseId=1']");
        page.waitForURL("**/vehicles**");

        Assertions.assertEquals(6, page.locator("tr").count());
        Assertions.assertEquals(1, page.locator(
                "h1:has-text('Список автомобилей предприятия')").count());
        Assertions.assertEquals(1, page.locator(
                "a[href*='/vehicles/new']:has-text('Добавить новый автомобиль')").count());
        Assertions.assertEquals(1, page.locator("tr:has-text('М123КА12')").count());
        page.close();
    }

    @Test
    @Order(3)
    public void deleteVehicleTest() {
        page.click("a[href*='/vehicles?enterpriseId=1']");
        page.waitForURL("**/vehicles**");

        Assertions.assertEquals(7, page.locator("tr").count());
        Assertions.assertEquals(1, page.locator("tr:has-text('A000AA01')").count());

        page.click("a[href*='/vehicles/delete/11']");
        page.waitForURL("**/vehicles**");

        Assertions.assertEquals(6, page.locator("tr").count());
        Assertions.assertEquals(0, page.locator("tr:has-text('A000AA01')").count());
        page.close();
    }

    @Test
    @Order(2)
    public void createVehicleTest() {
        page.click("a[href*='/vehicles?enterpriseId=1']");
        page.waitForURL("**/vehicles**");

        Assertions.assertEquals(6, page.locator("tr").count());
        Assertions.assertEquals(0, page.locator("tr:has-text('A000AA01')").count());

        page.click("a[href*='/vehicles/new']");
        page.waitForURL("**/vehicles/new");

        page.selectOption("[name=modelId]", "1");
        page.selectOption("[name=enterpriseId]", "1");
        page.fill("#licensePlateNumber", "A000AA01");
        page.fill("#mileageKm", "1000");
        page.fill("#productionYear", "2015");
        page.fill("#color", "White");
        page.fill("#purchasePriceRub", "1250000");
        page.fill("#purchaseDateTime", "2025-01-01T00:00");
        page.click("[type=submit]");

        page.waitForURL("**/vehicles?enterpriseId=1");
        Assertions.assertEquals(7, page.locator("tr").count());
        Assertions.assertEquals(1, page.locator("tr:has-text('A000AA01')").count());
        page.close();
    }

    @Test
    @Order(4)
    //Не забывать про ключ
    public void getVehicleTripsTest() {
        page.click("a[href*='/vehicles?enterpriseId=1']");
        page.waitForURL("**/vehicles**");

        page.click("a[href*='/vehicles/1']");
        page.waitForURL("**/vehicles/1");

        assertThat(page.locator("tr")).hasCount(2);
        assertThat(page.locator("#map")).isHidden();
        assertThat(page.locator("th:has-text('Адрес отправления')")).isHidden();

        page.fill("#dateFrom", "2025-01-01T00:00");
        page.fill("#dateTo", "2026-01-01T00:00");
        page.click("#submitButton");

        assertThat(page.locator("#map")).isVisible();
        assertThat(page.locator("th:has-text('Адрес отправления')")).isVisible();
        assertThat(page.locator("tr")).hasCount(5);
        page.close();
    }
}
