/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.payload;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
public class LoginPayload {

    private String username;
    private String password;
    private String newPassword;
    private String confirmPassword;
    private Boolean userOnlineStatus;
    private String password2;
    private String confirmPassword2;
    private String sessionId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Boolean getUserOnlineStatus() {
        return userOnlineStatus;
    }

    public void setUserOnlineStatus(Boolean userOnlineStatus) {
        this.userOnlineStatus = userOnlineStatus;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getConfirmPassword2() {
        return confirmPassword2;
    }

    public void setConfirmPassword2(String confirmPassword2) {
        this.confirmPassword2 = confirmPassword2;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

}
