package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.lessons.my.service.ImportService;

@RestController
@RequestMapping(value = "/api/v1/import")
@RequiredArgsConstructor
public class ImportRestController {

    private final ImportService importService;

    @PostMapping(path = "/enterprise", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importEnterprise(@RequestParam("file") MultipartFile file,
                                              @RequestParam("format") String format) {

        importService.importEnterprise(file, format);
        return ResponseEntity.ok().body("Import finished");
    }

    @PostMapping(path = "/vehicles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importVehicles(@RequestParam("file") MultipartFile file,
                                            @RequestParam("format") String format,
                                            @RequestParam("enterpriseId") long enterpriseId) {

        importService.importVehicles(file, format, enterpriseId);
        return ResponseEntity.ok().body("Import finished");
    }

    @PostMapping(path = "/trips", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importTrips(@RequestParam("file") MultipartFile file,
                                         @RequestParam("format") String format,
                                         @RequestParam("vehicleId") long vehicleId) {

        importService.importTrips(file, format, vehicleId);
        return ResponseEntity.ok().body("Import finished");
    }
}
