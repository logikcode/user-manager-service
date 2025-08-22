/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.service;

import com.accionmfb.accion.model.UssdAuditLog;
import com.accionmfb.accion.payload.AuditLogPayload;
import java.util.Collection;

/**
 *
 * @author bokon
 */
public interface UssdService {

    Collection<UssdAuditLog> getUSSDAuditLog(AuditLogPayload auditLogPayload);
}
