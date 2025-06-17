package com.drv.filestorage.security;

import com.drv.filestorage.config.ParameterStoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityProperties {

    @Value("${app.ssm.user-password}")
    private String passwordParameterName;

    private final ParameterStoreService parameterStoreService;

    public SecurityProperties(ParameterStoreService parameterStoreService) {
        this.parameterStoreService = parameterStoreService;
    }

    @Bean
    public String userPassword() {
        return parameterStoreService.getParameter(passwordParameterName);
    }
}
