package ru.lessons.my.converter;

import org.geojson.Feature;
import org.geojson.Point;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.lessons.my.model.entity.GeoPoint;
import ru.lessons.my.util.DateTimeUtils;

import java.time.ZoneId;
import java.util.Map;

@Component
public class GeoPointToFeatureConverter implements Converter<GeoPoint, Feature> {

    @Override
    public Feature convert(GeoPoint source) {
        ZoneId enterpriseTimeZone = ZoneId.of(source.getVehicle().getEnterprise().getTimeZone());
        Feature feature = new Feature();
        Point point = new Point(source.getPosition().getX(), source.getPosition().getY());

        feature.setGeometry(point);
        feature.setProperties(
                Map.of(
                        "vehicle_id", source.getVehicle().getId(),
                        "trip_id", source.getTrip() == null ? -1 : source.getTrip().getId(),
                        "visited_at", DateTimeUtils.convertFromUtc(source.getVisitedAt(), enterpriseTimeZone)
                )
        );
        return feature;
    }
}
