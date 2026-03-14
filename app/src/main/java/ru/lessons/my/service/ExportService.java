package ru.lessons.my.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import ru.lessons.my.converter.EnterpriseToEnterpriseDtoConverter;
import ru.lessons.my.converter.TripToTripDtoConverter;
import ru.lessons.my.converter.VehicleToVehicleDtoConverter;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.dto.ExportResult;
import ru.lessons.my.dto.TripDto;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.util.DateTimeUtils;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final EnterpriseService enterpriseService;
    private final VehicleService vehicleService;
    private final GeoService geoService;
    private final ObjectMapper defaultObjectMapper;
    private final EnterpriseToEnterpriseDtoConverter toEnterpriseDtoConverter;
    private final VehicleToVehicleDtoConverter toVehicleDtoConverter;
    private final TripToTripDtoConverter toTripDtoConverter;

    @SneakyThrows
    public ExportResult exportEnterprise(long enterpriseId, String format) {
        EnterpriseDto enterprise = toEnterpriseDtoConverter.convert(enterpriseService.findById(enterpriseId));

        if ("csv".equalsIgnoreCase(format)) {
            byte[] data = writeCsv(List.of(enterprise));
            return new ExportResult(data, "enterprise.csv", "text/csv");
        } else {
            byte[] data = defaultObjectMapper.writeValueAsBytes(List.of(enterprise));
            return new ExportResult(data, "enterprise.json", "application/octet-stream");
        }
    }

    @SneakyThrows
    public ExportResult exportVehicles(long enterpriseId, String format) {
        Enterprise enterprise = enterpriseService.findById(enterpriseId);

        List<VehicleDto> vehicles = vehicleService.findByEnterprises(List.of(enterprise)).stream()
                .map(toVehicleDtoConverter::convert)
                .toList();

        if ("csv".equalsIgnoreCase(format)) {
            byte[] data = writeCsv(vehicles);
            return new ExportResult(data, "enterprise" + enterpriseId + "vehicles.csv", "text/csv");
        } else {
            byte[] data = defaultObjectMapper.writeValueAsBytes(vehicles);
            return new ExportResult(data, "enterprise" + enterpriseId + "vehicles.json", "application/octet-stream");
        }
    }

    @SneakyThrows
    public ExportResult exportTrips(String format, long vehicleId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        Vehicle vehicle = vehicleService.findById(vehicleId);

        ZoneId enterpriseTimeZone = ZoneId.of(vehicle.getEnterprise().getTimeZone());
        LocalDateTime utcFrom = DateTimeUtils.convertToUtc(dateFrom, enterpriseTimeZone);
        LocalDateTime utcTo = DateTimeUtils.convertToUtc(dateTo, enterpriseTimeZone);

        List<TripDto> trips = geoService.getTripsByVehicleIdAndTimeRange(vehicleId, utcFrom, utcTo).stream()
                .map(toTripDtoConverter::convert)
                .toList();

        if ("csv".equalsIgnoreCase(format)) {
            byte[] data = writeCsv(trips);
            return new ExportResult(data, "vehicle" + vehicleId + "trips.csv", "text/csv");
        } else {
            byte[] data = defaultObjectMapper.writeValueAsBytes(trips);
            return new ExportResult(data, "vehicle" + vehicleId + "trips.json", "application/octet-stream");
        }
    }

    @SneakyThrows
    private <T> byte[] writeCsv(List<T> data) {
        try (StringWriter sw = new StringWriter()) {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(sw)
                    .withApplyQuotesToAll(false)
                    .build();

            beanToCsv.write(data);
            return sw.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

}
