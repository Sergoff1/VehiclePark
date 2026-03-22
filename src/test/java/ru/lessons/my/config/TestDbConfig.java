package ru.lessons.my.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class TestDbConfig {

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(PostgresContainer.getJdbcUrl());
        config.setUsername(PostgresContainer.getUsername());
        config.setPassword(PostgresContainer.getPassword());
        config.setDriverClassName(PostgresContainer.getDriverClassName());
        config.setMaximumPoolSize(5);
        return new HikariDataSource(config);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("ru.lessons.my.model");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .addHost("localhost")
                        .database("park")
                        .username(PostgresContainer.getUsername())
                        .password(PostgresContainer.getPassword())
                        .build()
        );
    }

    @Bean
    public DatabaseClient databaseClient() {
        return DatabaseClient.create(connectionFactory());
    }

    @Bean
    public R2dbcEntityTemplate entityTemplate() {
        return new R2dbcEntityTemplate(connectionFactory());
    }
}
