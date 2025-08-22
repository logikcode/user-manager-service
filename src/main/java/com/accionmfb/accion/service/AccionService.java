/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.service;

import com.accionmfb.accion.model.AuditLog;
import com.accionmfb.accion.model.TransHistory;
import com.accionmfb.accion.payload.AuditLogPayload;
import java.util.Collection;

/**
 *
 * @author bokon
 */
public interface AccionService {

    String getSystemParameter(String paramName);

    Collection<AuditLog> getAuditLog(AuditLogPayload auditPayload);

    Collection<TransHistory> getA24CoreAuditLog(AuditLogPayload auditPayload);
}
