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
@Table(name = "ussd_audit_log")
public class UssdAuditLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "inputter")
    private String inputter;
    @Column(name = "date_inputted")
    private LocalDateTime dateInputted;
    @Column(name = "authorizer")
    private String authorizer;
    @Column(name = "date_authorized")
    private LocalDateTime dateAuthorized;
    @Column(name = "initialValue")
    private String initialValue;
    @Column(name = "newValue")
    private String newValue;
    @Column(name = "trans_type")
    private String transType;
    @Column(name = "computerName")
    private String computerName;
    @Column(name = "mobile_number")
    private String mobileNumber;
    @Column(name = "account_name")
    private String accountName;
    @Column(name = "account_number")
    private String accountNumber;
    @Column(name = "date_created")
    private LocalDate dateCreated;
    @Column(name = "branch")
    private String branch;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInputter() {
        return inputter;
    }

    public void setInputter(String inputter) {
        this.inputter = inputter;
    }

    public LocalDateTime getDateInputted() {
        return dateInputted;
    }

    public void setDateInputted(LocalDateTime dateInputted) {
        this.dateInputted = dateInputted;
    }

    public String getAuthorizer() {
        return authorizer;
    }

    public void setAuthorizer(String authorizer) {
        this.authorizer = authorizer;
    }

    public LocalDateTime getDateAuthorized() {
        return dateAuthorized;
    }

    public void setDateAuthorized(LocalDateTime dateAuthorized) {
        this.dateAuthorized = dateAuthorized;
    }

    public String getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

}
