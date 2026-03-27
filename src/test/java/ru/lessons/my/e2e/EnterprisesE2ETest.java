package ru.lessons.my.e2e;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.lessons.my.BaseE2ETest;

public class EnterprisesE2ETest extends BaseE2ETest {

    @Test
    public void displayEnterprisesTest() {
        page.navigate(url("/enterprises"));
        page.waitForURL("**/login**");

        page.fill("#username", "manager1");
        page.fill("#password", "password");
        page.click("button[type='submit']");

        page.waitForURL("**/enterprises");

        Assertions.assertEquals(3, page.locator("table tbody tr").count());
        page.close();
    }

    @Test
    public void updateEnterpriseTest() {
        login();

        Assertions.assertEquals(1, page.locator("tr:has-text('Дорого и долго')").count());

        Assertions.assertEquals(0, page.locator("tr:has-text('newName')").count());

        page.click("a[href*='/enterprises/edit/1']");
        page.waitForURL("**/enterprises/edit/1");

        page.fill("#name", "newName");
        page.click("button[type='submit']");
        page.waitForURL("**/enterprises");

        Assertions.assertEquals(0, page.locator("tr:has-text('Дорого и долго')").count());

        Assertions.assertEquals(1, page.locator("tr:has-text('newName')").count());
        page.close();
    }

    @Test
    public void getEnterpriseReportTest() {
        login();

        page.click("a[href*='/reports']");
        page.waitForURL("**/reports");

        page.selectOption("#reportType", "ENTERPRISE_MILEAGE");
        page.selectOption("#enterpriseId", "1");
        page.selectOption("#period", "DAY");
        page.fill("#startDate", "2025-08-01");
        page.fill("#endDate", "2026-08-01");
        page.click("#submitButton");

        page.waitForURL("**/reports");

        Assertions.assertEquals(1, page.locator(
                "h2:has-text('Пробег всех автомобилей предприятия за период')").count());
        Assertions.assertEquals(3, page.locator("table tbody tr").count());
        Assertions.assertEquals(1, page.locator("tr:has-text('2025-09-21')").count());
        Assertions.assertEquals(1, page.locator("td:has-text('90')").count());
        page.close();
    }

}
