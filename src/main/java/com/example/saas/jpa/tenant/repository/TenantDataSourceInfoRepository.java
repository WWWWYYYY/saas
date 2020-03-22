package com.example.saas.jpa.tenant.repository;

import com.example.saas.jpa.tenant.vo.TenantDataSourceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TenantDataSourceInfoRepository extends JpaRepository<TenantDataSourceInfo, Long> {


    List<TenantDataSourceInfo> findByServerName(String appName);
}  
