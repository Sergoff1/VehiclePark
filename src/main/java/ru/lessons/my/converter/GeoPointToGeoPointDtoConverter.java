package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.GeoPointDto;
import ru.lessons.my.model.GeoPoint;
import ru.lessons.my.util.DateTimeUtils;

import java.time.ZoneId;

@Component
public class GeoPointToGeoPointDtoConverter implements Converter<GeoPoint, GeoPointDto> {

    @Override
    public GeoPointDto convert(GeoPoint source) {
        ZoneId enterpriseTimeZone = ZoneId.of(source.getVehicle().getEnterprise().getTimeZone());
        return GeoPointDto.builder()
                .vehicleId(source.getVehicle().getId())
                .longitude(source.getPosition().getX())
                .latitude(source.getPosition().getY())
                .visitedAt(DateTimeUtils.convertFromUtc(source.getVisitedAt(), enterpriseTimeZone))
                .build();
    }
}
