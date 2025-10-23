package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.VehicleModelDto;
import ru.lessons.my.model.entity.VehicleModel;

@Component
public class VehicleModelToVehicleModelDtoConverter implements Converter<VehicleModel, VehicleModelDto> {

    @Override
    public VehicleModelDto convert(VehicleModel source) {
        return VehicleModelDto.builder()
                .id(source.getId())
                .brandName(source.getBrandName())
                .modelName(source.getModelName())
                .type(source.getType())
                .fuelTankCapacity(source.getFuelTankCapacity())
                .loadCapacityKg(source.getLoadCapacityKg())
                .seatsNumber(source.getSeatsNumber())
                .build();
    }
}
