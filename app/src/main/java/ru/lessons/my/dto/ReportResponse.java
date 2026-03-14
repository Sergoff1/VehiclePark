package ru.lessons.my.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.lessons.my.model.ReportPeriod;
import ru.lessons.my.model.ReportType;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportResponse {

    private Long chatId;
    private boolean success;
    private ReportType type;
    private ReportPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<String, Integer> values;

}
