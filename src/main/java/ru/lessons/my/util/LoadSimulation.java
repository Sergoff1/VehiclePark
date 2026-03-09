package ru.lessons.my.util;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Session;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.holdFor;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.reachRps;
import static io.gatling.javaapi.http.HttpDsl.http;

public class LoadSimulation extends Simulation {

    private static final AtomicInteger incrementalId = new AtomicInteger(0);

    private static Session getNextId(Session session) {
        int id = incrementalId.getAndIncrement();
        return session.set("idField", id);
    }

    public LoadSimulation() {
        setUp(buildReadScenario()
                .injectOpen(
                        rampUsers(800).during(Duration.ofSeconds(10)),
                        constantUsersPerSec(800).during(Duration.ofMinutes(1))
                )
                .throttle(
                        reachRps(3000).in(Duration.ofMinutes(1)),
                        holdFor(Duration.ofMinutes(1))
                )
                .protocols(setupProtocol())).assertions(global().responseTime()
                .max()
                .lte(10000), global().successfulRequests()
                .percent()
                .gt(90d));
    }

    private static ScenarioBuilder buildReadScenario() {
        return CoreDsl.scenario("Get drivers and cars LoadTest")
                .exec(http("getVehicles").get("/api/v1/vehicles")
                        .header("Content-Type", "application/json"))
                .exec(http("getDrivers").get("/api/v1/drivers")
                        .header("Content-Type", "application/json"));
    }

    private static ScenarioBuilder buildWriteScenario() {
        return CoreDsl.scenario("Create drivers and cars LoadTest")
                .exec(LoadSimulation::getNextId)
                .exec(http("createVehicle").post("/api/v1/vehicles")
                        .header("Content-Type", "application/json")
                        .body(StringBody("""
                            {
                                "model_id": 1,
                                "enterprise_id": 4,
                                "license_plate_number": "#{idField}",
                                "production_year": 2000,
                                "mileage_km": 100000,
                                "color": "Тестовый",
                                "purchase_price_rub": 100,
                                "purchase_date_time": "2025-12-12 12:00:00"
                            }
                            """)))
                .exec(http("createDriver").post("/api/v1/drivers")
                        .header("Content-Type", "application/json")
                        .body(StringBody("""
                                {
                                    "enterprise_id": 4,
                                    "name": "name",
                                    "salary_rub": 1000
                                }
                                """)));
    }

    private static HttpProtocolBuilder setupProtocol() {
        return http.baseUrl("http://localhost:8080")
                .acceptHeader("application/json")
                .authorizationHeader("Bearer PutME")
                .maxConnectionsPerHost(10)
                .userAgentHeader("Performance Test");
    }
}
