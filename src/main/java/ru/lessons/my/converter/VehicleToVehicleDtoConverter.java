package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.entity.Driver;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.util.DateTimeUtils;

import java.time.ZoneId;
import java.util.List;

@Component
public class VehicleToVehicleDtoConverter implements Converter<Vehicle, VehicleDto> {

    @Override
    public VehicleDto convert(Vehicle source) {
        List<Long> driverIds = source.getDrivers().stream().map(Driver::getId).toList();
        ZoneId enterpriseTimeZone = ZoneId.of(source.getEnterprise().getTimeZone());

        return VehicleDto.builder()
                .id(source.getId())
                .modelId(source.getModel().getId())
                .licensePlateNumber(source.getLicensePlateNumber())
                .mileageKm(source.getMileageKm())
                .productionYear(source.getProductionYear())
                .color(source.getColor())
                .purchasePriceRub(source.getPurchasePriceRub())
                .purchaseDateTime(DateTimeUtils.convertFromUtc(source.getPurchaseDateTime(), enterpriseTimeZone))
                .activeDriverId(source.getActiveDriver() != null ? source.getActiveDriver().getId() : -1)
                .enterpriseId(source.getEnterprise().getId())
                .driverIds(driverIds)
                .build();
    }
}
