package ru.lessons.my.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class Report {

    private ReportType type;
    private ReportPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long vehicleId;
    private Long enterpriseId;
    private Map<String, Integer> values;

}
