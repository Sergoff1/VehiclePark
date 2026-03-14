package ru.lessons.my.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.lessons.my.converter.EnterpriseDtoToEnterpriseConverter;
import ru.lessons.my.converter.VehicleDtoToVehicleConverter;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.dto.TripDto;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.GeoPoint;
import ru.lessons.my.model.entity.Trip;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.util.DateTimeUtils;
import ru.lessons.my.util.GeoUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Transactional
    @SneakyThrows
    public boolean importGpxTrip(MultipartFile file, long vehicleId) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(file.getInputStream());
        document.getDocumentElement().normalize();

        NodeList trkptList = document.getElementsByTagName("trkpt");

        Vehicle vehicle = vehicleService.findById(vehicleId);
        String distanceM = document.getElementsByTagName("geotracker:length").item(0).getTextContent();

        Trip trip = new Trip();
        trip.setVehicle(vehicle);
        trip.setMileageKm((int) Math.round(Double.parseDouble(distanceM) / 1000));

        GeometryFactory geometryFactory = new GeometryFactory();
        List<GeoPoint> geoPoints = new ArrayList<>();
        for (int i = 0; i < trkptList.getLength(); i++) {
            Element point = (Element) trkptList.item(i);

            String timeStr = point.getElementsByTagName("time").item(0).getTextContent();
            LocalDateTime visitedAt = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME);
            double lat = Double.parseDouble(point.getAttribute("lat"));
            double lon = Double.parseDouble(point.getAttribute("lon"));

            GeoPoint geoPoint = GeoPoint.builder()
                    .vehicle(vehicle)
                    .position(geometryFactory.createPoint(new Coordinate(lon, lat)))
                    .visitedAt(visitedAt)
                    .trip(trip)
                    .build();

            geoPoints.add(geoPoint);
        }

        trip.setStartDate(geoPoints.getFirst().getVisitedAt());
        trip.setEndDate(geoPoints.getLast().getVisitedAt());

        if(geoService.isTripDatesOverlapExisting(vehicleId, trip.getStartDate(), trip.getEndDate())) {
            return false;
        }

        //Загнал себя в ловушку констрейнтами, хорошо бы провести рефакторинг, чтобы не было нужды дважды сохранять поездку.
        geoService.save(trip);
        geoPoints.forEach(geoService::save);
        trip.setStartPoint(geoPoints.getFirst());
        trip.setEndPoint(geoPoints.getLast());
        geoService.save(trip);
        return true;
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
