package com.example.saas.jpa.tenant.service;

import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * 多租户 liquibase
 */
@Component
@Log4j2
public class TenantLiquibaseService {



    @Autowired(required = false)
    private LiquibaseProperties liquibaseProperties;

    @Autowired
    @Qualifier("tenantLiquibase")
    private SpringLiquibase tenantLiquibase;
    /**
     * 使用liquibase 初始化数据库 （更新表结构）
     *
     * @param dataSource
     */
    public void initDatabaseBySpringLiquibase(DataSource dataSource) {
        log.debug("current DataSource:{}", dataSource);
        if (null == tenantLiquibase || null == liquibaseProperties) {
            log.warn("springLiquibase is null can't init multi");
            return;
        }
        try {
            log.info("start init multi by liquibase");
            tenantLiquibase.setDataSource(dataSource);
            tenantLiquibase.afterPropertiesSet();
            log.info("success init multi by liquibase");
        } catch (Exception e) {
            log.error("springLiquibase init multi failed, {}", dataSource, e);
        }
    }
}  
