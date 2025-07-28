package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.model.Enterprise;

@Component
public class EnterpriseDtoToEnterpriseConverter implements Converter<EnterpriseDto, Enterprise> {

    @Override
    public Enterprise convert(EnterpriseDto source) {
        return Enterprise.builder()
                .id(source.getId())
                .name(source.getName())
                .city(source.getCity())
                .build();
    }
}
