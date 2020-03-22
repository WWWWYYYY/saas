package com.example.saas.jpa.tenant.vo;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 *
 */
@Data
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    @Column(
            name = "gmt_create",
            nullable = false,
            updatable = false
    )
    protected ZonedDateTime gmtCreate;
    @Column(
            name = "gmt_modify"
    )
    protected ZonedDateTime gmtModify;

    @PrePersist
    private void beforeSave() {
        this.gmtCreate = ZonedDateTime.now();
        this.gmtModify = ZonedDateTime.now();
    }

    @PreUpdate
    private void beforeUpdate() {
        this.gmtModify = ZonedDateTime.now();
    }

}
