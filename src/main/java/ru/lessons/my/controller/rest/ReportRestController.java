package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.model.Report;
import ru.lessons.my.model.ReportPeriod;
import ru.lessons.my.model.ReportType;
import ru.lessons.my.service.ReportService;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/api/v1/reports")
@RequiredArgsConstructor
public class ReportRestController {

    private final ReportService reportService;

    @GetMapping
    public Report generateReport(@RequestParam("reportType") ReportType reportType,
                                 @RequestParam(name = "enterpriseId", required = false) Long enterpriseId,
                                 @RequestParam(name = "vehicleId", required = false) Long vehicleId,
                                 @RequestParam("period") ReportPeriod period,
                                 @RequestParam("startDate") LocalDate startDate,
                                 @RequestParam("endDate") LocalDate endDate) {

        return reportService.getReport(reportType, enterpriseId, vehicleId, period, startDate, endDate);
    }
}
