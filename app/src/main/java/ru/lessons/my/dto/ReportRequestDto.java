package ru.lessons.my.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.lessons.my.model.ReportPeriod;
import ru.lessons.my.model.ReportType;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequestDto {
    private ReportType reportType;
    private ReportPeriod reportPeriod;
    private String entityId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long chatId;
}
