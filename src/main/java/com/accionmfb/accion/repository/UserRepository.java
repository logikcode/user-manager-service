/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.repository;

import com.accionmfb.accion.model.AppRoles;
import com.accionmfb.accion.model.ApprovalLevel;
import com.accionmfb.accion.model.AuditLog;
import com.accionmfb.accion.model.Branch;
import com.accionmfb.accion.model.GroupRoles;
import com.accionmfb.accion.model.LandingPage;
import com.accionmfb.accion.model.Promotion;
import com.accionmfb.accion.model.RoleGroup;
import com.accionmfb.accion.model.SystemParameters;
import com.accionmfb.accion.model.UserMenu;
import com.accionmfb.accion.model.UserRole;
import com.accionmfb.accion.model.UserTemp;
import com.accionmfb.accion.model.Users;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
public interface UserRepository {

    List<Users> getDisabledUsers();

    //Audit log related service
    void createAuditLog(AuditLog auditLog);

    //Login related service
    Users findUserByEmailAndPassword(String email, String password);

    Users findUserByEmail(String email);

    Users getUserUsingUsername(String username);

    List<String> getBranches();

    String getParameter(String paramName);

    List<SystemParameters> getSystemParameters();

    List<String> getSystemParameterNames();

    SystemParameters getSystemParameterUsingName(String paramName);

    SystemParameters updateParameter(SystemParameters systemParameter);

    LocalDateTime getUserResetTime(String username);

    List<Users> getUserProfiles();

    Users checkIfUsernameExist(String username);

    Branch getBranchUsingName(String branchName);

    Users getUserProfileUsingId(Long id);

    Users createUserProfile(Users user);

    Users dropUserProfile(Users user);

    Users updateUser(Users user);

    List<UserRole> getRoleList(String appType);

    List<String> getRoles(String appType);

    List<String> getActiveUsers();

    List<String> getAllUsers();

    List<UserMenu> getUserMenus(String principal, String appType);

    List<UserMenu> getMenuList(String appType);

    UserMenu createUserMenu(UserMenu userMenu);

    List<String> getMenus(String appType);

    Collection<GroupRoles> getGroupRoles(String groupName);

    String getUserCurrentRoleGroup(String username, String appType);

    UserMenu getUserMenuUsingId(Long id);

    int dropUserMenu(UserMenu userMenu);

    AppRoles getAppRolesUsingRoleName(String roleName, String appType);

    UserMenu checkIfPrincipalHasMenu(Users user, String appType, String roleName);

    LandingPage getLandingPageUsingRoleName(String roleName, String appType);

    UserRole createUserRole(UserRole userRole);

    UserRole getUserRole(Users user, LandingPage landingPage);

    UserRole getUserRoleUsingId(Long id);

    UserRole updateUserRole(UserRole userRole);

    String getUserLandingPageForAPI(Users principal, String appType);

    //LandingPage getLandingPageUsingId(LandingPage landingPage);
    List<String> getUserMenusForAPI(Users principal, String appType);

    UserRole checkIfPrincipalHasRole(Users user, String roleName);

    String getBranchCode(String branchName);

    Collection<GroupRoles> getRolesGroupedByAppType(String appType);

    Collection<RoleGroup> getRoleGroupsByAppType(String appType);

    Collection<RoleGroup> getRoleGroupsByAppType(String roleGroup, String appType);

    Collection<String> getRoleGroupsNameByAppType(String appType);

    GroupRoles getGroupByGroupName(String groupName, String appType);

    GroupRoles dropGroupRole(GroupRoles group);

    RoleGroup dropRoleGroup(RoleGroup group);

    RoleGroup createRoleGroup(RoleGroup roleGroup);

    RoleGroup updateRoleGroup(RoleGroup roleGroup);

    Collection<UserMenu> checkIfRoleGroupIsInUseByUser(String groupName);

    RoleGroup getRoleGroupsByNameByAppType(String roleGroup, String appType);

    GroupRoles checkIfRoleExistInGroup(String groupName, String roleName, String appType);

    GroupRoles createGroupRole(GroupRoles groupRole);

    GroupRoles getGroupRolesUsignId(Long id);

    String getRoleGroupOfUser(String principal, String appType);

    List<String> getRolesForGroup(String groupName);

    UserMenu getUserRoleGroupUsingUsername(String username, String appType);

    UserMenu updateUserRoleGroup(UserMenu roleGroup);

    UserTemp createUserTemp(UserTemp userTemp);

    UserTemp updateUserTemp(UserTemp userTemp);

    UserTemp getUserTempUsingId(Long id);

    Collection<UserTemp> getPendingProfileApproval();

    UserTemp dropUserTemp(UserTemp userTemp);
    
    LocalDate getPasswordExpiryDate(String username);
    
    ApprovalLevel getApprovalLevelUsingLevelCode(String approvalLevel);
    
    Collection<ApprovalLevel> getApprovalLevel();
    
    ApprovalLevel createApprovalLevel(ApprovalLevel approvalLevel);
    
    ApprovalLevel updateApprovalLevel(ApprovalLevel approvalLevel);
    
    Collection<Promotion> getPromotionList();
    
    List<Users> getAuthorizersUsingBranch(String branchName);
    
}
