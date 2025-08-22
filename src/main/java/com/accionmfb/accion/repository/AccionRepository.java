/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.repository;

import com.accionmfb.accion.model.AuditLog;
import com.accionmfb.accion.model.SystemParameters;
import com.accionmfb.accion.model.TransHistory;
import java.time.LocalDate;
import java.util.Collection;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
public interface AccionRepository {

    SystemParameters getSystemParameterUsingName(String paramName);

    String getParameterValue(String paramName);

    Collection<AuditLog> getAuditLog(LocalDate startDate, LocalDate endDate);

    Collection<TransHistory> getA24CoreAuditLog(LocalDate startDate, LocalDate endDate);

}
