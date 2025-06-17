package com.drv.filestorage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DynamicDataSourceConfig {

    private final ParameterStoreService parameterStoreService;

    @Value("${app.ssm.db-url-param}")
    private String dbUrlParam;

    @Value("${app.ssm.db-username-param}")
    private String dbUsernameParam;

    @Value("${app.ssm.db-password-param}")
    private String dbPasswordParam;

    @Bean
    @Primary
    public DataSource dataSource() {
        String url = parameterStoreService.getParameter(dbUrlParam);
        String username = parameterStoreService.getParameter(dbUsernameParam);
        String password = parameterStoreService.getParameter(dbPasswordParam);

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}
