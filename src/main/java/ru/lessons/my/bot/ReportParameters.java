package ru.lessons.my.bot;

import lombok.Getter;
import lombok.Setter;
import ru.lessons.my.model.ReportPeriod;
import ru.lessons.my.model.ReportType;

import java.time.LocalDate;

@Getter
@Setter
public class ReportParameters {
    private ReportType reportType;
    private ReportPeriod reportPeriod;
    private Long entityId;
    private LocalDate startDate;
    private LocalDate endDate;
}
