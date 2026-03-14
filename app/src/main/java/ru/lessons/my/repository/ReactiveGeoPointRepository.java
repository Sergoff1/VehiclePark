package ru.lessons.my.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.lessons.my.model.entity.GeoPoint;

@Repository
@RequiredArgsConstructor
public class ReactiveGeoPointRepository {

    private final DatabaseClient databaseClient;

    public Mono<GeoPoint> save(GeoPoint geoPoint) {
        return databaseClient
                .sql("INSERT INTO geo_point (position, visited_at, trip_id, vehicle_id) VALUES (:pos, :visitedAt, :tripId, :vehicleId)")
                .bind("pos", geoPoint.getPosition())
                .bind("visitedAt", geoPoint.getVisitedAt())
                .bind("tripId", geoPoint.getTrip().getId())
                .bind("vehicleId", geoPoint.getVehicle().getId())
                .filter((statement, executeFunction) ->
                        statement.returnGeneratedValues("id").execute())
                .fetch()
                .first()
                .map(row -> {
                    geoPoint.setId((Long)row.get("id"));
                    return geoPoint;
                });
    }
}
