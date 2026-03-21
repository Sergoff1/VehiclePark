package ru.lessons.my.config;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresContainer {
    private static final PostgreSQLContainer<?> CONTAINER;

    static {
        CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse(
                "postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("park")
                .withUsername("test")
                .withPassword("test");

        CONTAINER.start();
    }

    private PostgresContainer() {
    }

    public static String getJdbcUrl() {
        return CONTAINER.getJdbcUrl();
    }

    public static String getUsername() {
        return CONTAINER.getUsername();
    }

    public static String getPassword() {
        return CONTAINER.getPassword();
    }

    public static String getDriverClassName() {
        return CONTAINER.getDriverClassName();
    }
}
