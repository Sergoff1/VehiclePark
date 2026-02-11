package ru.lessons.my.config;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@PropertySource("classpath:application.properties")
public class DbConfig {

    @Value("${db.driver}") private String dbDriver;
    @Value("${db.url}") private String dbUrl;
    @Value("${db.username}") private String dbUsername;
    @Value("${db.password}") private String dbPassword;
    @Value("${hibernate.hbm2ddl.auto}") private String hibernateHbm2ddl;
    @Value("${hibernate.show_sql}") private String hibernateShowSql;
    @Value("${hibernate.format_sql}") private String hibernateFormatSql;
    @Value("${hibernate.jdbc.time_zone}") private String hibernateTimeZone;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(dbDriver);
        ds.setUrl(dbUrl);
        ds.setUsername(dbUsername);
        ds.setPassword(dbPassword);
        return ds;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan("ru.lessons.my.model");

        Properties jpaProps = new Properties();
        jpaProps.put("hibernate.hbm2ddl.auto", hibernateHbm2ddl);
        jpaProps.put("hibernate.show_sql", hibernateShowSql);
        jpaProps.put("hibernate.format_sql", hibernateFormatSql);
        jpaProps.put("hibernate.jdbc.time_zone", hibernateTimeZone);
        emf.setJpaProperties(jpaProps);

        emf.setPersistenceProviderClass(org.hibernate.jpa.HibernatePersistenceProvider.class);

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
                        .host("localhost")
                        .port(5432)
                        .database("vehiclePark")
                        .username(dbUsername)
                        .password(dbPassword)
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
