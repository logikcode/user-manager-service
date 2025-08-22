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
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
@Repository
public class AccionRepositoryImpl implements AccionRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public SystemParameters getSystemParameterUsingName(String paramName) {
        TypedQuery<SystemParameters> query = em.createQuery("SELECT p FROM SystemParameters p WHERE p.paramName =:paramName", SystemParameters.class)
                .setParameter("paramName", paramName);
        List<SystemParameters> parameters = query.getResultList();
        if (parameters.isEmpty()) {
            return null;
        }
        return parameters.get(0);
    }

    @Override
    public String getParameterValue(String paramName) {
        TypedQuery<String> query = em.createQuery("SELECT p.paramValue FROM SystemParameters p WHERE p.paramName = :paramName", String.class)
                .setParameter("paramName", paramName);
        List<String> selectedParam = query.getResultList();
        if (selectedParam.isEmpty()) {
            return null;
        }
        return selectedParam.get(0);
    }

    @Override
    public Collection<AuditLog> getAuditLog(LocalDate startDate, LocalDate endDate) {
        TypedQuery<AuditLog> query = em.createQuery("SELECT a FROM AuditLog a WHERE CONVERT(DATE, a.dateCreated) BETWEEN :startDate AND :endDate", AuditLog.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        List<AuditLog> log = query.getResultList();
        if (log.isEmpty()) {
            return null;
        }
        return log;
    }

    @Override
    public Collection<TransHistory> getA24CoreAuditLog(LocalDate startDate, LocalDate endDate) {
        TypedQuery<TransHistory> query = em.createQuery("SELECT t FROM TransHistory t WHERE CONVERT(DATE, t.createdAt) BETWEEN :startDate AND :endDate", TransHistory.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        List<TransHistory> log = query.getResultList();
        if (log.isEmpty()) {
            return null;
        }
        return log;
    }

}
