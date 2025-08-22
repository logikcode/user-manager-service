/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.service;

import com.accionmfb.accion.model.ApprovalLevel;
import com.accionmfb.accion.model.AuditLog;
import com.accionmfb.accion.model.GroupRoles;
import com.accionmfb.accion.model.Promotion;
import com.accionmfb.accion.model.RoleGroup;
import com.accionmfb.accion.model.SystemParameters;
import com.accionmfb.accion.model.UserMenu;
import com.accionmfb.accion.model.UserRole;
import com.accionmfb.accion.model.UserTemp;
import com.accionmfb.accion.model.Users;
import com.accionmfb.accion.payload.AuditDetailsPayload;
import com.accionmfb.accion.payload.AuditLogPayload;
import com.accionmfb.accion.payload.LoginPayload;
import com.accionmfb.accion.payload.ParameterPayload;
import com.accionmfb.accion.payload.RolePayload;
import com.accionmfb.accion.payload.SingleFieldPayload;
import com.accionmfb.accion.payload.UserProfilePayload;
import com.accionmfb.accion.payload.UserRolePayload;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
public interface UserService {
    //Client login related services

    //void resetLoginTimeWait();
    String loginClient(String loginPayload);

    String getAuthorizationHeader();

    String getSignatureHeader();

    //Audit log related services
    void createAuditLog(AuditLog auditLog);

    //System parameters
    String getSystemParameter(String paramName);

    List<SystemParameters> getSystemParameters();

    List<String> getSystemParameterNames();

    String updateSystemParameter(ParameterPayload parameterPayload, String principal);

    //Audit transactions
    List<AuditLog> getAuditLogs(AuditLogPayload auditLogPayload);

    String getUserBranch(String principal);

    List<String> getBranches();

    String getCompanyCode(String branchName);

    String getBranchUsingAccountNumber(String accountNumber);

    String getPrincipalState(String branch);

    String getStateCodeUsingBranchName(String branchName);

    void lockUser(String username);

    LocalDateTime getUserResetTime(String username);

    List<Users> getUserProfiles();

    //New
    String updateRoleStatus(Long id);

    String updateRole(UserRolePayload userRole, String principal, String appType);

    String addUserProfile(UserProfilePayload profilePayload, String principal);

    String updateUserProfile(UserProfilePayload profilePayload, String principal);

    List<UserRole> getRoleList(String appType);

    List<String> getRoles(String appType);

    List<String> getActiveUsers();

    List<String> getAllUsers();

    AuditDetailsPayload getUserProfileUsingId(Long id);

    int[] getUserStatistics();

    //User related service
    List<String> getUserRole(String principal);

    String getUserLandingPageForAPI(String requestPayload);

    String getUserRolesForAPI(String requestPayload);

    String getBranchCodeForAPI(String requestPayload);

    String getPrincipalBranchesForAPI(String requestPayload);

    String getPrincipalLimitsForAPI(String requestPayload);

    String getPasswordChangeForAPI(String requestPayload);

    String getSystemParameterForAPI(String requestPayload);

    String getApprovalLevelForAPI(String requestPayload);
    
    String getUserDetailsForAPI(String requestPayload);

    String updateMenu(UserRolePayload userRole, String principal, String appType);

    List<UserMenu> getMenuList(String appType);

    List<String> getMenus(String appType);

    String changePassword(LoginPayload loginPayload, String principal);

    String changeDefaultPassword(LoginPayload loginPayload);

    Collection<RoleGroup> getRoleGroupsByAppType(String appType);

    Collection<String> getRoleGroupsNameByAppType(String appType);

    String updateRoleGroup(RolePayload rolePayload, String principal, String appType);

    String dropRoleGroup(SingleFieldPayload roleDropPayload, String principal);

    Collection<GroupRoles> getRolesGroupedByAppType(String appType);

    String addRoleToGroup(RolePayload rolePayload, String principal, String appType);

    Collection<String> getRoleGroups(String appType);

    String approveUserProfile(Long id, String principal);

    String dropUserProfile(Long id, String principal);

    Collection<UserTemp> getPendingApproval();

    void updateUserAsOnline(String payload);

    Collection<ApprovalLevel> getApprovalLevels();

    String updateApprovalLevel(UserProfilePayload userProfilePayload, String principal);

    Collection<Promotion> getPromotionList();

    String encryptPassword(LoginPayload loginPayload, String principal);

    String getUserTypeForAPI(String requestPayload);
    
    String getAuthorizersEmailForAPI(String requestPayload);
    
    String getUserListForAPI();
    
    String getUserPasswordTry(String username);
}
