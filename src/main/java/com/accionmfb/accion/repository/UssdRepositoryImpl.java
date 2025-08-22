/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.repository;

import com.accionmfb.accion.model.UssdAuditLog;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

/**
 *
 * @author bokon
 */
@Repository
public class UssdRepositoryImpl implements UssdRepository {

    @PersistenceContext()
    EntityManager em;

    @Override
    public Collection<UssdAuditLog> getUSSDAuditLog(LocalDate startDate, LocalDate endDate) {
        TypedQuery<UssdAuditLog> query = em.createQuery("SELECT l FROM UssdAuditLog l WHERE l.dateCreated BETWEEN :startDate AND :endDate ORDER BY l.dateCreated DESC", UssdAuditLog.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate);
        List<UssdAuditLog> logs = query.getResultList();
        if (logs.isEmpty()) {
            return null;
        }
        return logs;
    }

}
