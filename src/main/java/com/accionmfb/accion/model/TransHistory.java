/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Basic;
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
@Table(name = "trans_history")
public class TransHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Column(name = "posting_date")
    private LocalDate postingDate;
    @Column(name = "originating_branch")
    private String originatingBranch;
    @Column(name = "originating_branch_code")
    private String originatingBranchCode;
    @Column(name = "dr_account")
    private String drAccount;
    @Column(name = "dr_account_name")
    private String drAccountName;
    @Column(name = "dr_account_bvn")
    private String drAccountBVN;
    @Column(name = "dr_account_kyc_level")
    private String drAccountKYCLevel;
    @Column(name = "cr_account")
    private String crAccount;
    @Column(name = "cr_account_name")
    private String crAccountName;
    @Column(name = "cr_account_bvn")
    private String crAccountBVN;
    @Column(name = "cr_account_kyc_level")
    private String crAccountKYCLevel;
    @Column(name = "cr_account_inst_code")
    private String crAccountInstCode;
    @Column(name = "cr_account_bank_name")
    private String crAccountBankName;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "narration")
    private String narration;
    @Column(name = "dr_cr")
    private String drCr;
    @Column(name = "trans_type")
    private String transType;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "trans_ref")
    private String transRef;
    @Column(name = "approved_by")
    private String approvedBy;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "reject_reason")
    private String rejectReason;
    @Column(name = "rejected_by")
    private String rejectedBy;
    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;
    @Column(name = "enquiry_ref")
    private String enquiryRef;
    @Column(name = "destination_inst_code")
    private String destinationInstCode;
    @Column(name = "channel_code")
    private String channelCode;
    @Column(name = "t24_trans_ref")
    private String t24TransRef;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getPostingDate() {
        return postingDate;
    }

    public void setPostingDate(LocalDate postingDate) {
        this.postingDate = postingDate;
    }

    public String getOriginatingBranch() {
        return originatingBranch;
    }

    public void setOriginatingBranch(String originatingBranch) {
        this.originatingBranch = originatingBranch;
    }

    public String getOriginatingBranchCode() {
        return originatingBranchCode;
    }

    public void setOriginatingBranchCode(String originatingBranchCode) {
        this.originatingBranchCode = originatingBranchCode;
    }

    public String getDrAccount() {
        return drAccount;
    }

    public void setDrAccount(String drAccount) {
        this.drAccount = drAccount;
    }

    public String getCrAccount() {
        return crAccount;
    }

    public void setCrAccount(String crAccount) {
        this.crAccount = crAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getDrCr() {
        return drCr;
    }

    public void setDrCr(String drCr) {
        this.drCr = drCr;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTransRef() {
        return transRef;
    }

    public void setTransRef(String transRef) {
        this.transRef = transRef;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getRejectedBy() {
        return rejectedBy;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getDrAccountName() {
        return drAccountName;
    }

    public void setDrAccountName(String drAccountName) {
        this.drAccountName = drAccountName;
    }

    public String getDrAccountBVN() {
        return drAccountBVN;
    }

    public void setDrAccountBVN(String drAccountBVN) {
        this.drAccountBVN = drAccountBVN;
    }

    public String getDrAccountKYCLevel() {
        return drAccountKYCLevel;
    }

    public void setDrAccountKYCLevel(String drAccountKYCLevel) {
        this.drAccountKYCLevel = drAccountKYCLevel;
    }

    public String getCrAccountName() {
        return crAccountName;
    }

    public void setCrAccountName(String crAccountName) {
        this.crAccountName = crAccountName;
    }

    public String getCrAccountBVN() {
        return crAccountBVN;
    }

    public void setCrAccountBVN(String crAccountBVN) {
        this.crAccountBVN = crAccountBVN;
    }

    public String getCrAccountKYCLevel() {
        return crAccountKYCLevel;
    }

    public void setCrAccountKYCLevel(String crAccountKYCLevel) {
        this.crAccountKYCLevel = crAccountKYCLevel;
    }

    public String getCrAccountInstCode() {
        return crAccountInstCode;
    }

    public void setCrAccountInstCode(String crAccountInstCode) {
        this.crAccountInstCode = crAccountInstCode;
    }

    public String getCrAccountBankName() {
        return crAccountBankName;
    }

    public void setCrAccountBankName(String crAccountBankName) {
        this.crAccountBankName = crAccountBankName;
    }

    public String getEnquiryRef() {
        return enquiryRef;
    }

    public void setEnquiryRef(String enquiryRef) {
        this.enquiryRef = enquiryRef;
    }

    public String getDestinationInstCode() {
        return destinationInstCode;
    }

    public void setDestinationInstCode(String destinationInstCode) {
        this.destinationInstCode = destinationInstCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getT24TransRef() {
        return t24TransRef;
    }

    public void setT24TransRef(String t24TransRef) {
        this.t24TransRef = t24TransRef;
    }

}
