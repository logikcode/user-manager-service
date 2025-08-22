/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.controller;

import com.accionmfb.accion.payload.AuditLogPayload;
import com.accionmfb.accion.service.AccionService;
import com.accionmfb.accion.service.UssdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author bokon
 */
@Controller
@RequestMapping("/audit")
public class AuditController {
    @Autowired
    AccionService accionService;
    @Autowired
    UssdService ussdService;
    private String alertMessage = "";

    @GetMapping("/user-manager/audit-log")
    public String userManagerAudit(@ModelAttribute("auditLogPayload") AuditLogPayload auditLogPayload, Model model) {
        model.addAttribute("auditLog", null);
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "usermanageraudit";
    }

    @PostMapping("/user-manager/get-audit-log")
    public String getUserManagerAudit(@ModelAttribute("auditLogPayload") AuditLogPayload auditLogPayload, Model model) {
        model.addAttribute("auditLog", accionService.getAuditLog(auditLogPayload));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "usermanageraudit";
    }

    @GetMapping("/user-manager/a24/audit-log")
    public String a24CoreAudit(@ModelAttribute("auditLogPayload") AuditLogPayload auditLogPayload, Model model) {
        model.addAttribute("auditLog", null);
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "a24audit";
    }

    @PostMapping("/user-manager/a24/get-audit-log")
    public String getA24CoreAudit(@ModelAttribute("auditLogPayload") AuditLogPayload auditLogPayload, Model model) {
        model.addAttribute("auditLog", accionService.getA24CoreAuditLog(auditLogPayload));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "a24audit";
    }

    @GetMapping("/user-manager/ussd/audit-log")
    public String ussdAudit(@ModelAttribute("auditLogPayload") AuditLogPayload auditLogPayload, Model model) {
        model.addAttribute("auditLog", null);
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "ussdaudit";
    }

    @PostMapping("/user-manager/ussd/get-audit-log")
    public String getUssdAudit(@ModelAttribute("auditLogPayload") AuditLogPayload auditLogPayload, Model model) {
        model.addAttribute("auditLog", ussdService.getUSSDAuditLog(auditLogPayload));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "ussdaudit";
    }

    private void resetAlertMessage() {
        alertMessage = "";
    }
}
