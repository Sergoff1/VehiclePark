package ru.lessons.my.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DriverDto {

    private Long id;
    private String name;
    private Double salaryRub;
    private Long enterpriseId;
    private Long currentVehicleId;
    private List<Long> vehicleIds;
}
