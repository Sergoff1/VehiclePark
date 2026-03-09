package ru.lessons.my.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.DriverDto;
import ru.lessons.my.model.entity.Driver;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.service.EnterpriseService;

@Component
@RequiredArgsConstructor
public class DriverDtoToDriverConverter implements Converter<DriverDto, Driver> {

    private final EnterpriseService enterpriseService;

    @Override
    public Driver convert(DriverDto source) {
        Enterprise enterprise = enterpriseService.findById(source.getEnterpriseId());

        return Driver.builder()
                .id(source.getId())
                .enterprise(enterprise)
                .name(source.getName())
                .salaryRub(source.getSalaryRub())
                .build();
    }
}
