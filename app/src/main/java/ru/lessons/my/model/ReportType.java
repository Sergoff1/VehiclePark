package ru.lessons.my.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReportType {

    VEHICLE_MILEAGE("Пробег автомобиля за период"),
    VEHICLE_TRIPS("Поездки автомобиля за период"),
    ENTERPRISE_TRIPS("Поездки всех автомобилей предприятия за период"),
    ENTERPRISE_MILEAGE("Пробег всех автомобилей предприятия за период");

    private final String description;
}
