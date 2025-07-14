package ru.lessons.my.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VehicleDto {

    private Long id;
    private long modelId;
    private Long enterpriseId;
    private Long activeDriverId;
    private String licensePlateNumber;
    private int productionYear;
    private int mileageKm;
    private String color;
    private int purchasePriceRub;
    private List<Long> driverIds;
}
