package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.DriverDto;
import ru.lessons.my.model.entity.Driver;
import ru.lessons.my.model.entity.Vehicle;

import java.util.List;

@Component
public class DriverToDriverDtoConverter implements Converter<Driver, DriverDto> {

    @Override
    public DriverDto convert(Driver source) {
        List<Long> vehicleIds = source.getVehicles().stream().map(Vehicle::getId).toList();

        return DriverDto.builder()
                .id(source.getId())
                .name(source.getName())
                .salaryRub(source.getSalaryRub())
                .enterpriseId(source.getEnterprise().getId())
                .currentVehicleId(source.getCurrentVehicle() != null ? source.getCurrentVehicle().getId() : -1)
                .vehicleIds(vehicleIds)
                .build();
    }
}
