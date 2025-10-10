package ru.lessons.my.util;

import com.graphhopper.api.GraphHopperGeocoding;
import com.graphhopper.api.model.GHGeocodingEntry;
import com.graphhopper.api.model.GHGeocodingRequest;
import com.graphhopper.api.model.GHGeocodingResponse;
import com.graphhopper.util.shapes.GHPoint;
import org.locationtech.jts.geom.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GeoUtils {

    private static final GraphHopperGeocoding ghGeocoding;

    static {
        ghGeocoding = new GraphHopperGeocoding();
        ghGeocoding.setKey(System.getenv("GH_KEY"));
    }

    public static String getAddressByPoint(Point point) {
        GHGeocodingRequest request = new GHGeocodingRequest(
                GHPoint.create(point),
                "",
                1);

        GHGeocodingResponse response = ghGeocoding.geocode(request);

        if (response.getHits().isEmpty()) {
            return "Не удалось определить адрес";
        }

        GHGeocodingEntry addressInfo = response.getHits().getFirst();

        List<String> addressParts = new ArrayList<>();
        addressParts.add(addressInfo.getHouseNumber());
        addressParts.add(addressInfo.getName());
        addressParts.add(addressInfo.getStreet());
        addressParts.add(addressInfo.getCity());
        addressParts.add(addressInfo.getState());
        addressParts.add(addressInfo.getCountry());

        return addressParts.stream()
                .filter(Objects::nonNull)
                .distinct() //Некоторые значения могут совпадать, поэтому избавимся от дубликатов.
                .collect(Collectors.joining(", "));
    }
}
