package com.example.saas.jpa.tenant;

import com.example.saas.jpa.tenant.service.TenantDataSourceService;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.UnknownUnwrapTypeException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * TODO
 */
public class MultiTenantConnectionProviderImpl extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {


    /**
     * 默认数据源
     *
     * @return
     */
    @Override
    protected DataSource selectAnyDataSource() {
        return selectDataSource("default");
    }

    /**
     * 自定义数据源
     * 不存在则返回默认
     *
     * @param tenantIdentifier
     * @return
     */
    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return TenantDataSourceService.DATA_SOURCE_MAP.get(tenantIdentifier);
    }

}