package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.dto.ExportResult;
import ru.lessons.my.service.ExportService;

import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/api/v1/export")
@RequiredArgsConstructor
public class ExportRestController {

    private final ExportService exportService;

    @GetMapping("/enterprise")
    public ResponseEntity<byte[]> exportEnterprise(@RequestParam("format") String format,
                                                   @RequestParam("enterpriseId") long enterpriseId) {

        ExportResult result = exportService.exportEnterprise(enterpriseId, format);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.data());
    }

    @GetMapping("/vehicles")
    public ResponseEntity<byte[]> exportVehicles(@RequestParam("format") String format,
                                                 @RequestParam("enterpriseId") long enterpriseId) {

        ExportResult result = exportService.exportVehicles(enterpriseId, format);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.data());
    }

    @GetMapping("/trips")
    public ResponseEntity<byte[]> exportTrips(@RequestParam("format") String format,
                                              @RequestParam("vehicleId") long vehicleId,
                                              @RequestParam("dateFrom") LocalDateTime dateFrom,
                                              @RequestParam("dateTo") LocalDateTime dateTo) {

        ExportResult result = exportService.exportTrips(format, vehicleId, dateFrom, dateTo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.data());
    }

}
