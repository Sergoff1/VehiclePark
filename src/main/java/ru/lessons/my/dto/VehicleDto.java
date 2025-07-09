package ru.lessons.my.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VehicleDto {

    private Long id;
    private long modelId;
    private String licensePlateNumber;
    private int productionYear;
    private int mileageKm;
    private String color;
    private int purchasePriceRub;
}
