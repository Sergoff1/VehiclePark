package ru.lessons.my.util;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.api.GraphHopperWeb;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import ru.lessons.my.model.GeoPoint;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.repository.GeoPointRepository;
import ru.lessons.my.repository.VehicleRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Command
@Slf4j
@RequiredArgsConstructor
public class TrackGenerator {

    private static final int GENERATION_DELAY_MS = 5000;

    private final GeoPointRepository geoPointRepository;
    private final VehicleRepository vehicleRepository;

    //generate-track -v 1 --area 55.732992 37.573300 55.776088 37.658373
    //todo Добавить параметр для ограничения количества точек
    @Command(description = "Create random track for vehicle")
    public String generateTrack(@Option(longNames = "vehicleId", shortNames = 'v', required = true) long vehicleId,
                                @Option(arityMin = 4, arityMax = 4, longNames = "area", description = "coordinates of the area within which the route will be built") double[] area) {

        //Далеко от места первого использования, но зато сразу пробросим ошибку, если указали неверный id. Важнее сэкономить запросы к API.
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException(String.format("Vehicle %s not found", vehicleId)));

        //todo Возможно есть смысл вынести в отдельный бин, но пока эта штука нужна лишь тут и возможно больше нигде не понадобится.
        GraphHopperWeb graphHopper = new GraphHopperWeb();
        graphHopper.setKey(System.getenv("GH_KEY"));

        Random random = new Random();
        GHRequest GHRequest = new GHRequest()
                .setProfile("car")
                .putHint("instructions", false)
                .addPoint(new GHPoint(random.nextDouble(area[0], area[2]), random.nextDouble(area[1], area[3])))
                .addPoint(new GHPoint(random.nextDouble(area[0], area[2]), random.nextDouble(area[1], area[3])));

        log.debug("send request to GraphHopper: {}", GHRequest);

        GHResponse fullRes = graphHopper.route(GHRequest);
        if (fullRes.hasErrors()) {
            throw new RuntimeException(fullRes.getErrors().toString());
        }

        log.info("API calls remaining: {}", fullRes.getHeader("x-ratelimit-remaining", "0"));

        PointList points = fullRes.getBest().getPoints();
        GeometryFactory geometryFactory = new GeometryFactory();
        for (int i = 0; i < points.size(); i++) {
            GeoPoint geoPoint = GeoPoint.builder()
                    .vehicle(vehicle)
                    .position(geometryFactory.createPoint(new Coordinate(points.getLon(i), points.getLat(i))))
                    .visitedAt(LocalDateTime.now())
                    .build();

            geoPointRepository.save(geoPoint);

            try {
                Thread.sleep(GENERATION_DELAY_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return String.format("%d points created for vehicle %d", points.size(), vehicleId);
    }
}
