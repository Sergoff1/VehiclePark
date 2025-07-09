package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.Vehicle;

@Component
public class VehicleToVehicleDtoConverter implements Converter<Vehicle, VehicleDto> {

    @Override
    public VehicleDto convert(Vehicle source) {
        return VehicleDto.builder()
                .id(source.getId())
                .modelId(source.getModel().getId())
                .licensePlateNumber(source.getLicensePlateNumber())
                .mileageKm(source.getMileageKm())
                .productionYear(source.getProductionYear())
                .color(source.getColor())
                .purchasePriceRub(source.getPurchasePriceRub())
                .build();
    }
}
