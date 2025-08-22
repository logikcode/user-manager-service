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
public class RolePayload {

    private String roleName;
    private String roleGroupName;
    private String roleGroup;
    private String roles;
    private String[] groupName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleGroupName() {
        return roleGroupName;
    }

    public void setRoleGroupName(String newGroupName) {
        this.roleGroupName = newGroupName;
    }

    public String[] getGroupName() {
        return groupName;
    }

    public void setGroupName(String[] groupName) {
        this.groupName = groupName;
    }

    public String getRoleGroup() {
        return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
        this.roleGroup = roleGroup;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

}
