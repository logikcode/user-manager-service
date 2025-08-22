/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.service;

import com.accionmfb.accion.model.AuditLog;
import com.accionmfb.accion.model.TransHistory;
import com.accionmfb.accion.payload.AuditLogPayload;
import com.accionmfb.accion.repository.AccionRepository;
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
public class AccionServiceImpl implements AccionService {

    @Autowired
    AccionRepository accionRepository;

    @Override
    public String getSystemParameter(String paramName) {
        return accionRepository.getParameterValue(paramName);
    }

    @Override
    public Collection<AuditLog> getAuditLog(AuditLogPayload auditPayload) {
        //Check if only  dates are selected
        Collection<AuditLog> log = accionRepository.getAuditLog(LocalDate.parse(auditPayload.getStartDate().trim()), 
                LocalDate.parse(auditPayload.getEndDate().trim()));

        //Check if the log is null
        if (log == null) {
            return null;
        }

        //Check if the username is supplied
        if (!auditPayload.getUsername().equals("")) {
            log = log.stream().filter(t -> t.getCreatedBy().equals(auditPayload.getUsername())).collect(Collectors.toList());
        }

        //Check the audit class
        if (!auditPayload.getAuditClass().equals("")) {
            if (auditPayload.getAuditClass().equals("Profile")) {
                log = log.stream().filter(t -> t.getAuditClass().equals("Profile")).collect(Collectors.toList());
            }
            if (auditPayload.getAuditClass().equals("Role")) {
                log = log.stream().filter(t -> t.getAuditClass().equals("Role")).collect(Collectors.toList());
            }
            if (auditPayload.getAuditClass().equals("Password")) {
                log = log.stream().filter(t -> t.getAuditClass().equals("Password")).collect(Collectors.toList());
            }
            if (auditPayload.getAuditClass().equals("Parameter")) {
                log = log.stream().filter(t -> t.getAuditClass().equals("Parameter")).collect(Collectors.toList());
            }
        }

        //Check the audit type
        if (!auditPayload.getAuditType().equals("")) {
            if (auditPayload.getAuditType().equals("All")) {
                log = log.stream().filter(t -> !t.getAuditType().equals("All")).collect(Collectors.toList());
            }
            if (auditPayload.getAuditType().equals("Create")) {
                log = log.stream().filter(t -> t.getAuditType().equals("Create")).collect(Collectors.toList());
            }
            if (auditPayload.getAuditType().equals("Update")) {
                log = log.stream().filter(t -> t.getAuditType().equals("Update")).collect(Collectors.toList());
            }
            if (auditPayload.getAuditType().equals("Delete")) {
                log = log.stream().filter(t -> t.getAuditType().equals("Delete")).collect(Collectors.toList());
            }
        }

        return log;
    }

    @Override
    public Collection<TransHistory> getA24CoreAuditLog(AuditLogPayload auditPayload) {
        Collection<TransHistory> log = accionRepository.getA24CoreAuditLog(LocalDate.parse(auditPayload.getStartDate().trim()), 
                LocalDate.parse(auditPayload.getEndDate().trim()));

        if (log == null) {
            return null;
        }

        //Check if the username is supplied
        if (!auditPayload.getAccountNumber().equals("")) {
            log = log.stream().filter(t -> t.getCrAccount().equals(auditPayload.getAccountNumber()) || t.getDrAccount().equals(auditPayload.getAccountNumber())).collect(Collectors.toList());
        }

        //Check if the username is supplied
        if (!auditPayload.getUsername().equals("")) {
            log = log.stream().filter(t -> t.getCreatedBy().equals(auditPayload.getUsername())|| t.getApprovedBy().equals(auditPayload.getUsername())).collect(Collectors.toList());
        }

        //Check if the branch is supplied
        if (!auditPayload.getBranch().equals("")) {
            log = log.stream().filter(t -> t.getOriginatingBranch().equals(auditPayload.getBranch())).collect(Collectors.toList());
        }

        //Check the audit class
        if (!auditPayload.getAuditClass().equals("")) {
            if (auditPayload.getAuditClass().equals("NIP")) {
                log = log.stream().filter(t -> t.getTransType().equals("NIP")).collect(Collectors.toList());
            }
            if (auditPayload.getAuditClass().equals("LocalTransfer")) {
                log = log.stream().filter(t -> t.getTransType().equals("Local")).collect(Collectors.toList());
            }
        }

        return log;
    }

}
