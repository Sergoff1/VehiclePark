package ru.lessons.my.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ReportRequestDto {
    private ReportType reportType;
    private ReportPeriod reportPeriod;
    private String entityId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long chatId;
}
