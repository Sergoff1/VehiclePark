package ru.lessons.my.controller;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
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
import java.util.Map;

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
        List<Vehicle> vehicles = vehicleService.findByEnterprises(enterprises);

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
            PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
            Document document = new Document(pdfDocument);
            PdfFont font = PdfFontFactory.createFont("static/fonts/arialuni.ttf", PdfEncodings.IDENTITY_H, PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            document.setFont(font);

            document.add(new Paragraph(report.getType().getDescription())
                    .setFont(font)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER));

            String paragraph = report.getVehicleId() != null
                    ? "Авто: " + report.getVehicleId()
                    : "Предприятие: " + report.getEnterpriseId();

            document.add(new Paragraph(paragraph)
                    .setFont(font)
                    .setFontSize(12));

            document.add(new Paragraph("Период: " + report.getPeriod().getName())
                    .setFont(font)
                    .setFontSize(12));

            document.add(new Paragraph("С: " + report.getStartDate())
                    .setFont(font)
                    .setFontSize(12));

            document.add(new Paragraph("По: " + report.getEndDate())
                    .setFont(font)
                    .setFontSize(12));

            Table table = new Table(2);
            table.setWidth(500);
            table.setMarginTop(20);

            String value = report.getType().toString().contains("TRIPS")
                    ? "Количество поездок"
                    : "Пробег, км";

            table.addHeaderCell(new Cell().add(new Paragraph("Дата").setFont(font)));
            table.addHeaderCell(new Cell().add(new Paragraph(value).setFont(font)));

            for (Map.Entry<String, Integer> entry : report.getValues().entrySet()) {
                table.addCell(new Cell().add(new Paragraph(entry.getKey()).setFont(font)));
                table.addCell(new Cell().add(new Paragraph(entry.getValue().toString()).setFont(font)));
            }

            document.add(table);
            document.close();
        }
    }
}
