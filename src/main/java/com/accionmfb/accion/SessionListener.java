/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion;

import com.accionmfb.accion.service.AccionService;
import com.accionmfb.accion.service.UserService;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
public class SessionListener implements HttpSessionListener {
    @Autowired
    AccionService accionService;
    @Autowired
    UserService userService;
    @Autowired
    Environment env;

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        se.getSession().setMaxInactiveInterval(new Integer(userService.getSystemParameter("Session_Timeout")));
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

    }

}
