package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.MileageReport;
import ru.lessons.my.model.Report;
import ru.lessons.my.model.ReportPeriod;
import ru.lessons.my.model.ReportType;
import ru.lessons.my.model.TripsReport;
import ru.lessons.my.model.entity.Trip;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BiFunction<ReportPeriod, LocalDate, String> getISODateByPeriod = (reportPeriod, localDate) ->
            switch (reportPeriod) {
                case DAY -> localDate.toString();
                case MONTH -> localDate.getYear() + "-" + localDate.getMonthValue();
                case YEAR -> String.valueOf(localDate.getYear());
            };

    private final GeoService geoService;

    public Report getReport(ReportType reportType,
                            Long enterpriseId,
                            Long vehicleId,
                            ReportPeriod period,
                            LocalDate startDate,
                            LocalDate endDate) {

        return switch (reportType) {
            case VEHICLE_TRIPS -> getVehicleTripsReport(vehicleId, period, startDate, endDate);
            case VEHICLE_MILEAGE -> getVehicleMileageReport(vehicleId, period, startDate, endDate);
            case ENTERPRISE_TRIPS -> getEnterpriseTripsReport(enterpriseId, period, startDate, endDate);
            case ENTERPRISE_MILEAGE -> getEnterpriseMileageReport(enterpriseId, period, startDate, endDate);
        };
    }

    private MileageReport getVehicleMileageReport(Long vehicleId, ReportPeriod period, LocalDate startDate, LocalDate endDate) {
        List<Trip> trips = geoService.getTripsByVehicleIdAndTimeRange(vehicleId, startDate.atStartOfDay(), endDate.atStartOfDay());

        Map<String, Integer> mileages = getTripsMileageByPeriod(period, trips);

        return new MileageReport(ReportType.VEHICLE_MILEAGE, period, startDate, endDate, vehicleId, null, mileages);
    }

    private MileageReport getEnterpriseMileageReport(Long enterpriseId, ReportPeriod period, LocalDate startDate, LocalDate endDate) {
        List<Trip> trips = geoService.getTripsByEnterpriseIdAndTimeRange(enterpriseId, startDate.atStartOfDay(), endDate.atStartOfDay());

        Map<String, Integer> mileages = getTripsMileageByPeriod(period, trips);

        return new MileageReport(ReportType.ENTERPRISE_MILEAGE, period, startDate, endDate, null, enterpriseId, mileages);
    }

    private TripsReport getVehicleTripsReport(Long vehicleId, ReportPeriod period, LocalDate startDate, LocalDate endDate) {
        List<Trip> trips = geoService.getTripsByVehicleIdAndTimeRange(vehicleId, startDate.atStartOfDay(), endDate.atStartOfDay());

        Map<String, Integer> tripsData = getTripsCountByPeriod(period, trips);

        return new TripsReport(ReportType.VEHICLE_TRIPS, period, startDate, endDate, vehicleId, null, tripsData);
    }

    private TripsReport getEnterpriseTripsReport(Long enterpriseId, ReportPeriod period, LocalDate startDate, LocalDate endDate) {
        List<Trip> trips = geoService.getTripsByEnterpriseIdAndTimeRange(enterpriseId, startDate.atStartOfDay(), endDate.atStartOfDay());

        Map<String, Integer> tripsData = getTripsCountByPeriod(period, trips);

        return new TripsReport(ReportType.ENTERPRISE_TRIPS, period, startDate, endDate, null, enterpriseId, tripsData);
    }

    private Map<String, Integer> getTripsCountByPeriod(ReportPeriod period, List<Trip> trips) {
        return trips.stream()
                .collect(Collectors.groupingBy(
                        t -> getISODateByPeriod.apply(period, t.getStartDate().toLocalDate()),
                        Collectors.reducing(0, t -> 1, Integer::sum)));
    }

    private Map<String, Integer> getTripsMileageByPeriod(ReportPeriod period, List<Trip> trips) {
        return trips.stream()
                .collect(Collectors.groupingBy(
                        t -> getISODateByPeriod.apply(period, t.getStartDate().toLocalDate()),
                        Collectors.summingInt(Trip::getMileageKm)));
    }

}
