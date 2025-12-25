package ru.lessons.my.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.lessons.my.model.Report;
import ru.lessons.my.model.ReportPeriod;
import ru.lessons.my.model.ReportType;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.service.EnterpriseService;
import ru.lessons.my.service.ReportService;
import ru.lessons.my.service.VehicleService;

import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SecurityUtils securityUtils;
    private final EnterpriseService enterpriseService;
    private final VehicleService vehicleService;

    @GetMapping
    public String showGenerateForm(Model model) {
        Manager manager = securityUtils.getCurrentManager();
        List<Enterprise> enterprises = enterpriseService.findByManager(manager);
        //todo Опасная штука, при большом количестве автомобилей. Может руками задавать айдишник?
        List<Vehicle> vehicles = vehicleService.findByManager(manager);

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("enterprises", enterprises);
        model.addAttribute("reportTypes", ReportType.values());
        model.addAttribute("periods", ReportPeriod.values());
        return "reports/generate";
    }

    @PostMapping
    public String generateReport(@RequestParam("reportType") ReportType reportType,
                                 @RequestParam(name = "enterpriseId", required = false) Long enterpriseId,
                                 @RequestParam(name = "vehicleId", required = false) Long vehicleId,
                                 @RequestParam("period") ReportPeriod period,
                                 @RequestParam("startDate") LocalDate startDate,
                                 @RequestParam("endDate") LocalDate endDate,
                                 Model model) {

        Report report = reportService.getReport(reportType, enterpriseId, vehicleId, period, startDate, endDate);
        model.addAttribute("report", report);
        return "reports/show";
    }

    @GetMapping("/pdf")
    public void exportToPdf(@RequestParam("reportType") ReportType reportType,
                            @RequestParam(name = "enterpriseId", required = false) Long enterpriseId,
                            @RequestParam(name = "vehicleId", required = false) Long vehicleId,
                            @RequestParam("period") ReportPeriod period,
                            @RequestParam("startDate") LocalDate startDate,
                            @RequestParam("endDate") LocalDate endDate,
                            HttpServletResponse response) throws Exception {

        Report report = reportService.getReport(reportType, enterpriseId, vehicleId, period, startDate, endDate);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"");
        try (OutputStream outputStream = response.getOutputStream()) {
            reportService.writePdfToOutputStream(outputStream, report);
        }
    }
}
