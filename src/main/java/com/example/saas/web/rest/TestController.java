package com.example.saas.web.rest;

import com.example.saas.jpa.tenant.repository.TenantDataSourceInfoRepository;
import com.example.saas.jpa.tenant.vo.TenantDataSourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * TODO
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Value("${spring.application.name}")
    private String appName;
    @Autowired
    private TenantDataSourceInfoRepository tenantDataSourceInfoRepository;

    @GetMapping("/index")
    public List<TenantDataSourceInfo> index(){
        List<TenantDataSourceInfo> list = tenantDataSourceInfoRepository.findByServerName(appName);
        return list;
    }
}  
