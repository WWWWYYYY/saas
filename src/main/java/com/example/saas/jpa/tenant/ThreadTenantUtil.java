package com.example.saas.jpa.tenant;

/**
 * TODO
 */
public class ThreadTenantUtil {
    private static ThreadLocal<String> threadLocal = new ThreadLocal();

    public ThreadTenantUtil() {
    }

    public static void setTenant(String tenantCode) {
        if (null != tenantCode) {
            threadLocal.set(tenantCode);
        }

    }

    public static String getTenant() {
        return (String)threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
