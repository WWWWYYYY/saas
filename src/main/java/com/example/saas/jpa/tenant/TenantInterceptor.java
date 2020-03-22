package com.example.saas.jpa.tenant;

import com.example.saas.jpa.tenant.service.TenantDataSourceService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO
 */
@Log4j2
public class TenantInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String tenantCode = this.getTenantCode(request);
        log.debug("TenantInterceptor setTenant:{}", tenantCode);
        if (StringUtils.isBlank(tenantCode) ) {
            log.warn("current_tenant_code_not_found : uri -> {}", request.getRequestURI());
            throw new Exception("租户号不存在！");
        }
        if (TenantDataSourceService.DATA_SOURCE_MAP.get(tenantCode)==null){
            throw new Exception("租户不存在！");
        }
//        if (tenantDataSourceService.checkDataSource(tenantCode, null)) {
//            tenantDataSourceService.initDataSourceInfoByTenantCode(tenantCode, Constants.PROFILE_MYSQL);
//            tenantDataSourceService.initDataSourceInfoByTenantCode(tenantCode, Constants.PROFILE_MONGO);
//        }
        // mongo he mysql 都没有配置则报异常
        //设置租户信息
        ThreadTenantUtil.setTenant(tenantCode);
        return true;
    }

    private String getTenantCode(HttpServletRequest request) {
        String tenantCode = request.getHeader("tenant_code");
        return tenantCode;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                @Nullable Exception ex) throws Exception {
        log.debug("TenantInterceptor removeTenant:{}", ThreadTenantUtil.getTenant());
        //释放资源
        ThreadTenantUtil.remove();
    }
}  
