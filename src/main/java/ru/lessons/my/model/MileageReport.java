package ru.lessons.my.model;

import java.time.LocalDate;
import java.util.Map;

public class MileageReport extends Report {

    public MileageReport(ReportType type,
                         ReportPeriod period,
                         LocalDate startDate,
                         LocalDate endDate,
                         Long vehicleId,
                         Long enterpriseId,
                         Map<String, Integer> values) {
        super(type, period, startDate, endDate, vehicleId, enterpriseId, values);
    }
}
