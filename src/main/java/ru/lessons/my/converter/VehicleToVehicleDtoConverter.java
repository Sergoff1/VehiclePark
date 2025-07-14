package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.Driver;
import ru.lessons.my.model.Vehicle;

import java.util.List;

@Component
public class VehicleToVehicleDtoConverter implements Converter<Vehicle, VehicleDto> {

    @Override
    public VehicleDto convert(Vehicle source) {
        List<Long> driverIds = source.getDrivers().stream().map(Driver::getId).toList();

        return VehicleDto.builder()
                .id(source.getId())
                .modelId(source.getModel().getId())
                .licensePlateNumber(source.getLicensePlateNumber())
                .mileageKm(source.getMileageKm())
                .productionYear(source.getProductionYear())
                .color(source.getColor())
                .purchasePriceRub(source.getPurchasePriceRub())
                .activeDriverId(source.getActiveDriver() != null ? source.getActiveDriver().getId() : -1)
                .enterpriseId(source.getEnterprise().getId())
                .driverIds(driverIds)
                .build();
    }
}
