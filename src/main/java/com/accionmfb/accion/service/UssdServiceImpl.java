/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.service;

import com.accionmfb.accion.model.UssdAuditLog;
import com.accionmfb.accion.payload.AuditLogPayload;
import com.accionmfb.accion.repository.UssdRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author bokon
 */
@Service
public class UssdServiceImpl implements UssdService {

    @Autowired
    UssdRepository ussdRepository;

    @Override
    public Collection<UssdAuditLog> getUSSDAuditLog(AuditLogPayload auditLogPayload) {
        Collection<UssdAuditLog> auditLog = ussdRepository.getUSSDAuditLog(LocalDate.parse(auditLogPayload.getStartDate()), LocalDate.parse(auditLogPayload.getEndDate()));
        if (auditLog == null) {
            return null;
        }

        //Filter by the branch
        if (!auditLogPayload.getBranch().equals("")) {
            auditLog = auditLog.stream().filter(t -> t.getBranch().equals(auditLogPayload.getBranch())).collect(Collectors.toList());
        }

        //Filter by username
        if (!auditLogPayload.getUsername().equals("")) {
            auditLog = auditLog.stream().filter(t -> t.getInputter().equals(auditLogPayload.getUsername())).collect(Collectors.toList());
        }

        //Filter by the account or mobile number
        if (!auditLogPayload.getAccountNumber().equals("")) {
            if (auditLogPayload.getAccountNumber().length() == 11) {
                auditLog = auditLog.stream().filter(t -> t.getMobileNumber().equals(auditLogPayload.getAccountNumber())).collect(Collectors.toList());
            }
            if (auditLogPayload.getAccountNumber().length() == 10) {
                auditLog = auditLog.stream().filter(t -> t.getAccountNumber().equals(auditLogPayload.getAccountNumber())).collect(Collectors.toList());
            }
        }

        //Filter the transaction by type
        if (auditLogPayload.getAuditClass().equals("Pin Reset")) {
            auditLog = auditLog.stream().filter(t -> t.getTransType().equals("Pin Reset")).collect(Collectors.toList());
        }

        if (auditLogPayload.getAuditClass().equals("Mobile Number Change")) {
            auditLog = auditLog.stream().filter(t -> t.getTransType().equals("Mobile Number Change")).collect(Collectors.toList());
        }

        if (auditLogPayload.getAuditClass().equals("Daily Limit")) {
            auditLog = auditLog.stream().filter(t -> t.getTransType().equals("Daily Limit")).collect(Collectors.toList());
        }

        if (auditLogPayload.getAuditClass().equals("Trans Limit")) {
            auditLog = auditLog.stream().filter(t -> t.getTransType().equals("Trans Limit")).collect(Collectors.toList());
        }

        if (auditLogPayload.getAuditClass().equals("Debit Card")) {
            auditLog = auditLog.stream().filter(t -> t.getTransType().equals("Debit Card")).collect(Collectors.toList());
        }

        if (auditLogPayload.getAuditClass().equals("Cheque Book")) {
            auditLog = auditLog.stream().filter(t -> t.getTransType().equals("Cheque Book")).collect(Collectors.toList());
        }

        if (auditLogPayload.getAuditClass().equals("Loan")) {
            auditLog = auditLog.stream().filter(t -> t.getTransType().equals("Loan")).collect(Collectors.toList());
        }

        return auditLog;
    }

}
