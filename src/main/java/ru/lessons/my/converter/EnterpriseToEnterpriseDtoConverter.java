package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.model.entity.Driver;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.model.entity.Vehicle;

import java.util.List;

@Component
public class EnterpriseToEnterpriseDtoConverter implements Converter<Enterprise, EnterpriseDto> {

    @Override
    public EnterpriseDto convert(Enterprise source) {
        List<Long> driverIds = source.getDrivers() == null
                ? List.of()
                : source.getDrivers().stream().map(Driver::getId).toList();
        List<Long> vehicleIds = source.getVehicles() == null
                ? List.of()
                : source.getVehicles().stream().map(Vehicle::getId).toList();
        List<Long> managerIds = source.getManagers() == null
                ? List.of()
                : source.getManagers().stream().map(Manager::getId).toList();

        return EnterpriseDto.builder()
                .id(source.getId())
                .name(source.getName())
                .city(source.getCity())
                .driverIds(driverIds)
                .vehicleIds(vehicleIds)
                .managerIds(managerIds)
                .build();
    }
}
