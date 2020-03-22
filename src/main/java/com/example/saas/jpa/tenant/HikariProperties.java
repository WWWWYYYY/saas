package com.example.saas.jpa.tenant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * TODO
 */
@Component
@ConfigurationProperties(prefix = "spring.datasource.hikari", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class HikariProperties {

    @Value("${spring.datasource.hikari.maximum-pool-size:}")
    private Integer maximumPoolSize;
    @Value("${spring.datasource.hikari.minimum-idle:}")
    private Integer minimumIdle;
    @Value("${spring.datasource.hikari.auto-commit:true}")
    private Boolean autoCommit;

    private Properties dataSourceProperties;

    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public Integer getMinimumIdle() {
        return minimumIdle;
    }

    public void setMinimumIdle(Integer minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public Boolean getAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(Boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public Properties getDataSourceProperties() {
        return dataSourceProperties;
    }

    public void setDataSourceProperties(Properties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }
}
