package com.example.saas.jpa.tenant.service;

import com.example.saas.jpa.tenant.HikariProperties;
import com.example.saas.jpa.tenant.vo.TenantDataSourceInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.util.DriverDataSource;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 多租户 数据源管理
 */
@Log4j2
@Service
public class TenantDataSourceService {

    @Value("${spring.application.name}")
    private String APP_NAME;

    public static final Map<String, DataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<>();

    public static final String DEFAULT_KEY = "default";

    private static final String MYSQL_JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String QUERY_SCHEMA_SQL = "SELECT count(sc.SCHEMA_NAME) FROM information_schema.SCHEMATA sc where sc.SCHEMA_NAME=?";
    private static final String CREATE_DATABASE = "create database ";
    private static final String DEFAULT_CHARACTER_SET = " DEFAULT  character set ";
    private static final String MYSQL_SPEC = "`";
    private static final String USE = "use ";
    private static final String ALTER_DATABASE = "alter database ";
    private static final String CHARACTER_SET = " character set ";
    private static final String defaultCharset = " utf8mb4";
    @Autowired
    private HikariProperties hikariProperties;
    @Autowired
    private Environment env;

    public static final String SUFFIX = "_DataSource";

    @Autowired
    private DefaultListableBeanFactory defaultListableBeanFactory;


    @Autowired
    private TenantLiquibaseService tenantLiquibaseService;

    /**
     * 创建数据库 并切换
     *
     * @param dataSourceInfo
     * @return
     */
    public boolean createAndChangeDb(TenantDataSourceInfo dataSourceInfo) {

        String dbName = dataSourceInfo.getDatabase();
        boolean result = false;
        try (
            Connection conn = DriverManager.getConnection(dataSourceInfo.getUrl(), dataSourceInfo.getUsername(), dataSourceInfo.getPassword())
        ) {
            log.info("dbName:{}", dbName);
            PreparedStatement ps = conn.prepareStatement(QUERY_SCHEMA_SQL);
            ps.setString(1, dbName);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                log.info("db:{} is not exist, system auto create:{}", dbName, dbName);


                String createSQL = CREATE_DATABASE + MYSQL_SPEC + dbName + MYSQL_SPEC + DEFAULT_CHARACTER_SET + defaultCharset;

                log.info("createSQL:{}", createSQL);
                conn.createStatement().execute(createSQL);
                result = true;
            }
            conn.createStatement().execute(USE + MYSQL_SPEC + dbName + MYSQL_SPEC);
        } catch (Exception e) {
            log.error("connect to DB:{} failed, connection:{}, exception:{}", dbName, e);
        }
        return result;
    }

    /**
     * 初始化数据源
     *
     * @param dataSourceInfo
     */
    public void initDatabase(TenantDataSourceInfo dataSourceInfo) {
        log.info("init multi database :{} ", dataSourceInfo);
        DataSource dataSource = null;
        try {
            //尝试连接租户指定数据源
            dataSource = genDataSource(dataSourceInfo);
            tenantLiquibaseService.initDatabaseBySpringLiquibase(dataSource);
        } catch (Exception e) {
            //异常则在默认数据源同实例下创建一个数据库
            log.error("connect to tenant DB failed,{} ", e);
            createAndChangeDb(dataSourceInfo);
            if (null == dataSource) {
                dataSource=genDataSource(dataSourceInfo);
            }
            tenantLiquibaseService.initDatabaseBySpringLiquibase(dataSource);
        }finally {
            if (dataSource!=null){
                TenantDataSourceService.DATA_SOURCE_MAP.put(dataSourceInfo.getTenantCode(),dataSource);
            }
        }
    }

    /**
     * 创建数据源
     *
     * @param dataSourceInfo
     * @return
     */
    public DataSource genDataSource(TenantDataSourceInfo dataSourceInfo) {
        log.info("addDataSource:{}", dataSourceInfo);
        Assert.notNull(dataSourceInfo, "tenantInfo is null");
        Assert.hasText(dataSourceInfo.getUrl(), "url is empty");
        Assert.hasText(dataSourceInfo.getUsername(), "username is empty");
        Assert.hasText(dataSourceInfo.getTenantCode(), "tenantCode is empty");
        if (null != dataSourceInfo.getDatabase()) {
            Assert.isTrue(dataSourceInfo.getUrl().contains(dataSourceInfo.getDatabase()), "url is not include multi");
        }
        HikariDataSource defaultHikariDataSource = (HikariDataSource) DATA_SOURCE_MAP.get(DEFAULT_KEY);
        defaultHikariDataSource.getDataSourceProperties();
        HikariConfig hikariConfig = new HikariConfig();
        if (null != hikariProperties.getMaximumPoolSize()) {
            hikariConfig.setMaximumPoolSize(hikariProperties.getMaximumPoolSize());
        } else {
            hikariConfig.setMaximumPoolSize(defaultHikariDataSource.getMaximumPoolSize());
        }
        if (null != hikariProperties.getMinimumIdle()) {
            hikariConfig.setMinimumIdle(hikariProperties.getMinimumIdle());
        } else {
            hikariConfig.setMinimumIdle(defaultHikariDataSource.getMinimumIdle());
        }
        //线下环境 配置
        if (isDevEnv()) {
            hikariConfig.setIdleTimeout(30000);
            hikariConfig.setConnectionTimeout(60000);
            hikariConfig.setValidationTimeout(3000);
            hikariConfig.setMaxLifetime(60000);
        }
        hikariConfig.setJdbcUrl(dataSourceInfo.getUrl());
        hikariConfig.setUsername(dataSourceInfo.getUsername());
        hikariConfig.setPassword(dataSourceInfo.getPassword());
        hikariConfig.setAutoCommit(hikariProperties.getAutoCommit());
        hikariConfig.setDriverClassName(StringUtils.isNotBlank(dataSourceInfo.getJdbcDriver()) ? dataSourceInfo.getJdbcDriver() : MYSQL_JDBC_DRIVER);
        if (null == hikariProperties.getDataSourceProperties()) {
            hikariProperties.setDataSourceProperties(hikariConfig.getDataSourceProperties());
        }
        hikariConfig.setDataSource(new DriverDataSource(
                dataSourceInfo.getUrl(),
                StringUtils.isNotBlank(dataSourceInfo.getJdbcDriver()) ? dataSourceInfo.getJdbcDriver() : MYSQL_JDBC_DRIVER,
                hikariProperties.getDataSourceProperties(),
                dataSourceInfo.getUsername(),
                dataSourceInfo.getPassword()
        ));
        return new HikariDataSource(hikariConfig);
    }


    private boolean isDevEnv() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        return !activeProfiles.contains("prod");
    }


    /**
     * 添加数据源到上下文
     *
     * @param dataSourceInfo
     */
    public void addDataSource(TenantDataSourceInfo dataSourceInfo) {
        log.debug("addDataSource:{}", dataSourceInfo);
        if (null == dataSourceInfo) {
            log.warn("datasource is empty");
            return;
        }
        synchronized (dataSourceInfo.getTenantCode()) {
            if (DATA_SOURCE_MAP.containsKey(dataSourceInfo.getTenantCode())) {
//                if (BooleanUtils.isTrue(dataSourceInfo.getNeedOverride())) {
//                    removeDataSource(dataSourceInfo.getTenantCode());
//                }
                return;
            }
            DataSource hikariDataSource = genDataSource(dataSourceInfo);
            DATA_SOURCE_MAP.put(dataSourceInfo.getTenantCode(), hikariDataSource);
            String beanName = dataSourceInfo.getTenantCode() + SUFFIX;
            this.registerBean(beanName, hikariDataSource);
        }
    }


    /**
     * 将数据源bean注册到上下文
     *
     * @param beanName
     * @param hikariDataSource
     */
    private void registerBean(String beanName, DataSource hikariDataSource) {
        try {
            // 动态注入Bean
            if (defaultListableBeanFactory.containsBean(beanName)) {
                //移除bean的定义和实例
                defaultListableBeanFactory.removeBeanDefinition(beanName);
            }
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(hikariDataSource.getClass());
            beanDefinitionBuilder.setDestroyMethodName("close");
            defaultListableBeanFactory.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
        } catch (Exception e) {
            log.error("registerBean failed", e);
        }
    }

    /**
     * 删除数据源
     *
     * @param tenantCode
     */
    public static void removeDataSource(String tenantCode) {
        if (StringUtils.isBlank(tenantCode)) {
            return;
        }
        if (DATA_SOURCE_MAP.containsKey(tenantCode) && !DEFAULT_KEY.equalsIgnoreCase(tenantCode)) {
            ((HikariDataSource) DATA_SOURCE_MAP.get(tenantCode)).close();
            DATA_SOURCE_MAP.remove(tenantCode);
        }
    }
}  
