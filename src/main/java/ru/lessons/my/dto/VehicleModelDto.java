package ru.lessons.my.dto;

import lombok.Builder;
import lombok.Getter;
import ru.lessons.my.model.entity.VehicleModel;

@Getter
@Builder
public class VehicleModelDto {

    private Long id;
    private String brandName;
    private String modelName;
    private VehicleModel.Type type;
    private Integer fuelTankCapacity;
    private Integer loadCapacityKg;
    private Integer seatsNumber;
}
