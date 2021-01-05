package com.axamit.springboot.demo.demorestservice.config;

import com.axamit.springboot.demo.demorestservice.DemoRestServiceApplication;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class AppConfig {

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    private @Value("${postgresql.secrets.path}") String postgresqlSecretsPath;
    private @Value("${postgresql.user.file}") String postgresqlUserFile;
    private @Value("${postgresql.password.file}") String postgresqlPasswordFile;
    private @Value("${spring.datasource.url}") String dataSourceUrl;
    private @Value("${spring.datasource.driver-class-name}") String driverClassName;

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

                logger.info("Configuring datasource...");

                HikariDataSource dataSource = new HikariDataSource();

                String postgresqlUser = sanitize(Files.readAllBytes(
                        Paths.get(postgresqlSecretsPath, postgresqlUserFile)));
                String postgresqlPassword = sanitize(Files.readAllBytes(
                        Paths.get(postgresqlSecretsPath, postgresqlPasswordFile)));

                dataSource.setUsername(postgresqlUser);
                dataSource.setPassword(postgresqlPassword);
                dataSource.setJdbcUrl(dataSourceUrl);
                dataSource.setDriverClassName(driverClassName);

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
