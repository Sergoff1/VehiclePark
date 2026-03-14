package ru.lessons.my.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.dto.TripDto;
import ru.lessons.my.model.entity.Trip;
import ru.lessons.my.util.DateTimeUtils;
import ru.lessons.my.util.GeoUtils;

import java.time.ZoneId;

@Component
public class TripToTripDtoConverter implements Converter<Trip, TripDto> {

    @Override
    public TripDto convert(Trip source) {
        ZoneId enterpriseTimeZone = ZoneId.of(source.getVehicle().getEnterprise().getTimeZone());

        return TripDto.builder()
                .id(source.getId())
                .vehicleId(source.getVehicle().getId())
                .startTime(DateTimeUtils.convertFromUtc(source.getStartDate(), enterpriseTimeZone))
                .endTime(DateTimeUtils.convertFromUtc(source.getEndDate(), enterpriseTimeZone))
                .startPointId(source.getStartPoint().getId())
                .endPointId(source.getEndPoint().getId())
                .startAddress(GeoUtils.getAddressByPoint(source.getStartPoint().getPosition()))
                .endAddress(GeoUtils.getAddressByPoint(source.getEndPoint().getPosition()))
                .build();
    }
}
