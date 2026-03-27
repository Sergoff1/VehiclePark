package ru.lessons.my.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.lessons.my.BaseIntegrationTest;
import ru.lessons.my.model.Report;
import ru.lessons.my.model.ReportPeriod;
import ru.lessons.my.model.ReportType;

import java.time.LocalDate;
import java.util.Map;

public class ReportServiceTest extends BaseIntegrationTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void generateVehicleMileageReportTest() {
        Report report = reportService.getReport(
                ReportType.VEHICLE_MILEAGE,
                null,
                1L,
                ReportPeriod.DAY,
                LocalDate.of(2025,9, 1),
                LocalDate.now()
        );

        Map<String, Integer> expectedValues = Map.of("2025-09-21", 90, "2025-10-22", 82);

        Assertions.assertEquals(ReportType.VEHICLE_MILEAGE, report.getType());
        Assertions.assertEquals(ReportPeriod.DAY, report.getPeriod());
        Assertions.assertEquals(LocalDate.of(2025,9, 1), report.getStartDate());
        Assertions.assertEquals(LocalDate.now(), report.getEndDate());
        Assertions.assertEquals(1L, report.getVehicleId());
        Assertions.assertEquals(expectedValues, report.getValues());
        Assertions.assertNull(report.getEnterpriseId());
    }

}
