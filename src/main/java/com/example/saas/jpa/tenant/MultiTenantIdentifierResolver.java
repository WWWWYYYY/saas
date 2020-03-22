package com.example.saas.jpa.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import java.util.Optional;

/**
 * TODO
 */
public class MultiTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    /**
     * 从获取当前线程租户信息
     *
     * @return 数据源名称
     */
    @Override
    public String resolveCurrentTenantIdentifier() {
        return Optional.ofNullable(ThreadTenantUtil.getTenant()).orElse("default");
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}