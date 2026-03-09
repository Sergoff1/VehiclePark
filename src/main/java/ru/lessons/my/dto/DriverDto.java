package ru.lessons.my.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDto {

    private Long id;
    private String name;
    private Double salaryRub;
    private Long enterpriseId;
    private Long currentVehicleId;
    private List<Long> vehicleIds;
}
