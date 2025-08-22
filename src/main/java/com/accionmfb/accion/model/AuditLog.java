/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
@Entity
@Table(name = "audit_log")
public class AuditLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "date_created")
    private LocalDateTime dateCreated;
    @Column(name = "approved_by")
    private String approvedBy;
    @Column(name = "date_approved")
    private LocalDateTime dateApproved;
    @Column(name = "system_date")
    private LocalDateTime systemDate;
    @Column(name = "audit_type")
    private String auditType;
    @Column(name = "audit_class")
    private String auditClass;
    @Column(name = "computerName")
    private String computerName;
    @Column(name = "action_description", length = 500)
    private String actionDescription;
    @Column(name = "app_type")
    private String appType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuditType() {
        return auditType;
    }

    public void setAuditType(String auditType) {
        this.auditType = auditType;
    }

    public String getAuditClass() {
        return auditClass;
    }

    public void setAuditClass(String auditClass) {
        this.auditClass = auditClass;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getSystemDate() {
        return systemDate;
    }

    public void setSystemDate(LocalDateTime systemDate) {
        this.systemDate = systemDate;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getDateApproved() {
        return dateApproved;
    }

    public void setDateApproved(LocalDateTime dateApproved) {
        this.dateApproved = dateApproved;
    }
    
}
