package com.example.saas.jpa.tenant.service;

import com.example.saas.jpa.tenant.TenantInterceptor;
import com.example.saas.jpa.tenant.ThreadTenantUtil;
import com.example.saas.jpa.tenant.repository.TenantDataSourceInfoRepository;
import com.example.saas.jpa.tenant.vo.TenantDataSourceInfo;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.saas.jpa.tenant.service.TenantDataSourceService.DATA_SOURCE_MAP;

/**
 *
 */
@Log4j2
@Component
public class TenantInitService extends WebMvcConfigurerAdapter implements ApplicationRunner, ServletContextInitializer {


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
    /**
     * 多租户过滤器不过滤url
     */
    private final String[] excludes = {
            "/index.html",
            "/management/**",
            "/v2/api-docs",
            "/swagger-resources/**",
            "/swagger-ui**",
            "/webjars/**",
            "/error/**",
            "/api/multi-tenancy/**",
            "/api/common/kafka/**",
            "/app-operation/**",
            "/health/**"
    };


    @Value("${tenancy.interceptor.excludes:}")
    private String userExcludes;
//    @Autowired(required = false)
//    private Set<TenantMissHandleInterface> tenantMissHandleInterfaces;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.debug("add TenantInterceptor");
        // 请求拦截
        List<String> allExcludes = new ArrayList<>();
        allExcludes.addAll(Arrays.asList(excludes));
        if (StringUtils.hasText(userExcludes)) {
            log.debug("server has config excludes of {}", userExcludes);
            allExcludes.addAll(Arrays.asList(userExcludes.split(",")));
        }
        registry.addInterceptor(new TenantInterceptor())
                .addPathPatterns("/**").excludePathPatterns(allExcludes.toArray(new String[allExcludes.size()]));
    }
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        //添加默认数据源
        TenantDataSourceService.DATA_SOURCE_MAP.put("default",dataSource);
        tenantLiquibaseService.initDatabaseBySpringLiquibase(dataSource);

        //添加当前租户，配置表中的数据源
        List<TenantDataSourceInfo> byServerName = tenantDataSourceInfoRepository.findByServerName(appName);
        byServerName.stream().forEach(tenantDataSourceService::initDatabase);
    }
}
