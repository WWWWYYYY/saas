package com.example.saas.jpa.tenant.vo;



import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * A TenantDataSourceInfo.
 */
@Data
@Entity
@Table(name = "tenant_data_source_info")
public class TenantDataSourceInfo extends AbstractEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url")
    private String url;

    @Column(name = "jhi_database")
    private String database;

    @Column(name = "username")
    private String username;

    @Column(name = "jhi_password")
    private String password;

    @Column(name = "tenant_code")
    private String tenantCode;

    @Column(name = "jdbc_driver")
    private String jdbcDriver;

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "status")
    private Integer status;

    @Column(name = "delete_status")
    private Integer deleteStatus;

    /**
     * 数据源类型 ： mysql | mongo
     */
    @Column(name = "jhi_type")
    private String type;


}
