package com.example.saas.jpa.tenant.service;

import com.example.saas.jpa.tenant.repository.TenantDataSourceInfoRepository;
import com.example.saas.jpa.tenant.vo.TenantDataSourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.util.List;

import static com.example.saas.jpa.tenant.service.TenantDataSourceService.DATA_SOURCE_MAP;

/**
 *
 */
@Component
public class TenantInitService implements ApplicationRunner, ServletContextInitializer {


    @Autowired
    private TenantDataSourceService tenantDataSourceService;

    @Autowired
    private TenantDataSourceInfoRepository tenantDataSourceInfoRepository;
    @Autowired
    private DataSource dataSource;
    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private TenantLiquibaseService tenantLiquibaseService;
    @Override
    public void run(ApplicationArguments args) throws Exception {

    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        TenantDataSourceService.DATA_SOURCE_MAP.put("default",dataSource);
        tenantLiquibaseService.initDatabaseBySpringLiquibase(dataSource);

        List<TenantDataSourceInfo> byServerName = tenantDataSourceInfoRepository.findByServerName(appName);
        byServerName.stream().forEach(tenantDataSourceService::initDatabase);
    }
}
