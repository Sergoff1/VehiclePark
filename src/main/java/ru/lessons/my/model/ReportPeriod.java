package ru.lessons.my.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportPeriod {
    DAY("День"),
    MONTH("Месяц"),
    YEAR("Год");

    private final String name;

    public static ReportPeriod fromName(String name) {
        for (ReportPeriod period : ReportPeriod.values()) {
            if (period.name.equalsIgnoreCase(name)) {
                return period;
            }
        }
        return null;
    }
}
