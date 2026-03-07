package ru.lessons.my.util;

import io.gatling.javaapi.core.CoreDsl;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.http.HttpDsl.http;

public class LoadSimulation extends Simulation {

    public LoadSimulation() {
        setUp(buildScenario()
                .injectOpen(
                        rampUsers(500).during(Duration.ofSeconds(10)),
                        constantUsersPerSec(500).during(Duration.ofMinutes(1))
                )
                .protocols(setupProtocol())).assertions(global().responseTime()
                .max()
                .lte(10000), global().successfulRequests()
                .percent()
                .gt(90d));
    }

    private static ScenarioBuilder buildScenario() {
        return CoreDsl.scenario("Get drivers and cars LoadTest")
                .exec(http("getVehicles").get("/api/v1/vehicles")
                        .header("Content-Type", "application/json"))
                .exec(http("getDrivers").get("/api/v1/drivers")
                        .header("Content-Type", "application/json"));
    }

    private static HttpProtocolBuilder setupProtocol() {
        return http.baseUrl("http://localhost:8080")
                .acceptHeader("application/json")
                .authorizationHeader("Bearer putTokenHere")
                .maxConnectionsPerHost(10)
                .userAgentHeader("Performance Test");
    }
}
