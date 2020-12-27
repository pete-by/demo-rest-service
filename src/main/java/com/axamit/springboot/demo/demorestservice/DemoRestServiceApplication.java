package com.axamit.springboot.demo.demorestservice;

import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGConnectionPoolDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootApplication
public class DemoRestServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(DemoRestServiceApplication.class);

    private @Value("${postgresql.secrets.path}") String postgresqlSecretsPath;

    public static void main(String[] args) {
        SpringApplication.run(DemoRestServiceApplication.class, args);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
        PropertySourcesPlaceholderConfigurer propsConfig
                = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);
        return propsConfig;
    }

    @Bean
    public DataSource dataSource() {

        try {
            if (postgresqlSecretsPath != null && Files.exists(Paths.get(postgresqlSecretsPath))) {

                logger.info("Datasource Configuration...");

                HikariDataSource dataSource = new HikariDataSource();

                String postgresqlUser = sanitize(Files.readAllBytes(Paths.get(postgresqlSecretsPath, "postgresql-user")));
                String postgresqlPassword = sanitize(Files.readAllBytes(Paths.get(postgresqlSecretsPath, "postgresql-password")));

                dataSource.setUsername(postgresqlUser);
                dataSource.setPassword(postgresqlPassword);
                dataSource.setJdbcUrl("jdbc:postgresql://demo-postgres-postgresql:5432/demo_rest_service_db");
                dataSource.setDriverClassName("org.postgresql.Driver");

                return dataSource;
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private String sanitize(byte[] strBytes) {
        return new String(strBytes, StandardCharsets.US_ASCII)
                .replace("\r", "")
                .replace("\n", "");
    }

}