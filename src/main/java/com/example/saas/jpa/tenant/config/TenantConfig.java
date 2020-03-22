package com.example.saas.jpa.tenant.config;

import com.example.saas.jpa.tenant.TenantSpringLiquibase;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多租户配置类
 */
@Configuration
public class TenantConfig {

    @Autowired(required = false)
    private LiquibaseProperties liquibaseProperties;

    @Value("${liquibase.enable:false}")
    private boolean liquibaseEnable;

    @Bean("tenantLiquibase")
    public SpringLiquibase tenantLiquibase() {
        SpringLiquibase liquibase = new TenantSpringLiquibase();
        if (null == liquibaseProperties) {
            return liquibase;
        }
        liquibase.setChangeLog("classpath:liquibase/master.xml");
        liquibase.setContexts(liquibaseProperties.getContexts());
        liquibase.setDefaultSchema(liquibaseProperties.getDefaultSchema());
        liquibase.setDropFirst(liquibaseProperties.isDropFirst());
        liquibase.setChangeLogParameters(liquibaseProperties.getParameters());
        Boolean shouldRun = liquibaseEnable || liquibaseProperties.isEnabled();
        liquibase.setShouldRun(shouldRun);
        return liquibase;
    }
}  
