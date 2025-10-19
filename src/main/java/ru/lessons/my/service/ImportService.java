package ru.lessons.my.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.lessons.my.converter.EnterpriseDtoToEnterpriseConverter;
import ru.lessons.my.converter.VehicleDtoToVehicleConverter;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.dto.TripDto;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.GeoPoint;
import ru.lessons.my.model.Trip;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.util.DateTimeUtils;
import ru.lessons.my.util.GeoUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImportService {

    private final EnterpriseService enterpriseService;
    private final VehicleService vehicleService;
    private final GeoService geoService;
    private final ObjectMapper defaultObjectMapper;
    private final VehicleDtoToVehicleConverter toVehicleConverter;
    private final EnterpriseDtoToEnterpriseConverter toEnterpriseConverter;
    private final SecurityUtils securityUtils;

    @Transactional
    @SneakyThrows
    public void importEnterprise(MultipartFile file, String format) {
        List<EnterpriseDto> enterprises = "csv".equalsIgnoreCase(format)
                ? parseCsv(file.getInputStream(), EnterpriseDto.class)
                : parseJson(file.getInputStream(), EnterpriseDto.class);

        Enterprise enterprise = toEnterpriseConverter.convert(enterprises.getFirst());
        enterprise.setId(null);
        securityUtils.getCurrentManager().getEnterprises().add(enterprise);
        enterpriseService.save(enterprise);
    }

    @Transactional
    @SneakyThrows
    public void importVehicles(MultipartFile file, String format, long enterpriseId) {
        List<VehicleDto> vehicleDtos = "csv".equalsIgnoreCase(format)
                ? parseCsv(file.getInputStream(), VehicleDto.class)
                : parseJson(file.getInputStream(), VehicleDto.class);

        List<Vehicle> vehicles = vehicleDtos.stream()
                .map(v -> {
                    //В новом предприятии скорее всего таких водителей не будет, потому просто занулим их.
                    // Пока что, так как требования по импорту туманны.
                    v.setDriverIds(null);
                    v.setActiveDriverId(null);
                    v.setId(null);
                    v.setEnterpriseId(enterpriseId);
                    return toVehicleConverter.convert(v);
                })
                .toList();

        vehicles.forEach(vehicleService::save);
    }

    @Transactional
    @SneakyThrows
    public void importTrips(MultipartFile file, String format, long vehicleId) {
        List<TripDto> tripDtos = "csv".equalsIgnoreCase(format)
                ? parseCsv(file.getInputStream(), TripDto.class)
                : parseJson(file.getInputStream(), TripDto.class);

        List<Trip> trips = tripDtos.stream()
                .map(t -> {
                    Vehicle vehicle = vehicleService.findById(vehicleId);
                    ZoneId enterpriseTimeZone = ZoneId.of(vehicle.getEnterprise().getTimeZone());

                    Trip trip = new Trip();
                    trip.setVehicle(vehicle);
                    trip.setStartDate(DateTimeUtils.convertFromUtc(t.getStartTime(), enterpriseTimeZone));
                    trip.setEndDate(DateTimeUtils.convertFromUtc(t.getEndTime(), enterpriseTimeZone));

                    GeoPoint startPoint = GeoPoint.builder()
                            .vehicle(vehicle)
                            .trip(trip)
                            .visitedAt(trip.getStartDate())
                            .position(GeoUtils.getPointByAddress(t.getStartAddress()))
                            .build();

                    GeoPoint endPoint = GeoPoint.builder()
                            .vehicle(vehicle)
                            .trip(trip)
                            .visitedAt(trip.getEndDate())
                            .position(GeoUtils.getPointByAddress(t.getEndAddress()))
                            .build();

                    trip.setStartPoint(startPoint);
                    trip.setEndPoint(endPoint);
                    return trip;
                })
                .toList();

        trips.forEach(geoService::save);
    }

    private <T> List<T> parseCsv(InputStream is, Class<T> type) throws Exception {
        try (Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            CsvToBean<T> csvToBean = new CsvToBeanBuilder<T>(reader)
                    .withType(type)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();
        }
    }

    private <T> List<T> parseJson(InputStream is, Class<T> type) throws Exception {
        JavaType javaType = defaultObjectMapper.getTypeFactory().constructParametricType(List.class, type);
        return defaultObjectMapper.readValue(is, javaType);
    }
}
