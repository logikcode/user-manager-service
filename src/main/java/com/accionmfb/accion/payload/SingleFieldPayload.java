/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.payload;

import java.math.BigDecimal;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
public class SingleFieldPayload {
    private BigDecimal amount;
    private String fieldName;
    private int id;
    private String status;

    public SingleFieldPayload() {
    }

    public SingleFieldPayload(int id, String fieldName, String status) {
        this.fieldName = fieldName;
        this.id = id;
        this.status = status;
    }

    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    
}
