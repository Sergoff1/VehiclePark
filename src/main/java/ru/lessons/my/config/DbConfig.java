package ru.lessons.my.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
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
        jpaProps.put("hibernate.jdbc.time_zone", "UTC");
        emf.setJpaProperties(jpaProps);

        emf.setPersistenceProviderClass(org.hibernate.jpa.HibernatePersistenceProvider.class);

        return emf;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
