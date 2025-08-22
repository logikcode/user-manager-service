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
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    EntityManager em;

    @Override
    public LocalDateTime getUserResetTime(String username) {
        TypedQuery<LocalDateTime> query = em.createQuery("SELECT s.resetTime FROM Users s WHERE s.username = :username", LocalDateTime.class)
                .setParameter("username", username);
        List<LocalDateTime> resetTime = query.getResultList();
        if (resetTime.isEmpty()) {
            return null;
        }
        return resetTime.get(0);
    }

    @Override
    public List<Users> getDisabledUsers() {
        TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.status = 'Disabled'", Users.class);
        List<Users> users = query.getResultList();
        if (users.isEmpty()) {
            return null;
        }
        return users;
    }

    @Override
    @Transactional
    public void createAuditLog(AuditLog auditLog) {
        em.persist(auditLog);
        em.flush();
    }

    @Override
    public String getParameter(String paramName) {
        TypedQuery<String> query = em.createQuery("SELECT p.paramValue FROM SystemParameters p WHERE p.paramName = :paramName", String.class)
                .setParameter("paramName", paramName);
        List<String> selectedParam = query.getResultList();
        if (selectedParam.isEmpty()) {
            return null;
        }
        return selectedParam.get(0);
    }

    @Override
    @Transactional
    public SystemParameters updateParameter(SystemParameters systemParameter) {
        em.merge(systemParameter);
        em.flush();
        return systemParameter;
    }

    @Override
    public List<SystemParameters> getSystemParameters() {
        TypedQuery<SystemParameters> query = em.createQuery("SELECT p FROM SystemParameters p", SystemParameters.class);
        List<SystemParameters> parameters = query.getResultList();
        if (parameters.isEmpty()) {
            return null;
        }
        return parameters;
    }

    @Override
    public List<String> getSystemParameterNames() {
        TypedQuery<String> query = em.createQuery("SELECT p.paramName FROM SystemParameters p", String.class);
        List<String> parameters = query.getResultList();
        if (parameters.isEmpty()) {
            return null;
        }
        return parameters;
    }

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
    public List<String> getBranches() {
        TypedQuery<String> query = em.createQuery("SELECT b.branchName FROM Branch b", String.class);
        List<String> branch = query.getResultList();
        if (branch.isEmpty()) {
            return null;
        }
        return branch;
    }

    @Override
    public Users findUserByEmail(String username) {
        //This query will return an integer value of 1 if the email exist else 0
        TypedQuery<Users> query = em.createQuery("SELECT c FROM Users c WHERE c.username = :username", Users.class)
                .setParameter("username", username);
        List<Users> user = query.getResultList();
        if (user.isEmpty()) {
            return null;
        }
        return user.get(0);
    }

    @Override
    public Users findUserByEmailAndPassword(String username, String password) {
        //This query will return an object of Users or null
        TypedQuery<Users> query = em.createQuery("SELECT c FROM Users c WHERE c.username = :username AND c.password = :password", Users.class)
                .setParameter("username", username)
                .setParameter("password", password);
        List<Users> requestedUser = query.getResultList();
        if (requestedUser.isEmpty()) {
            return null;
        }
        return requestedUser.get(0);
    }

    @Override
    public Users getUserUsingUsername(String username) {
        //This query will return an object of Users or null
        TypedQuery<Users> query = em.createQuery("SELECT c FROM Users c WHERE c.username = :username", Users.class)
                .setParameter("username", username);
        List<Users> requestedUser = query.getResultList();
        if (requestedUser.isEmpty()) {
            return null;
        }
        return requestedUser.get(0);
    }

    @Override
    public List<UserMenu> getUserMenus(String principal, String appType) {
        //This query will return an object of Users or null
        TypedQuery<UserMenu> query = em.createQuery("SELECT u FROM UserMenu u WHERE u.username = :principal AND u.appType = :appType", UserMenu.class)
                .setParameter("principal", principal)
                .setParameter("appType", appType);
        List<UserMenu> requestedMenus = query.getResultList();
        if (requestedMenus.isEmpty()) {
            return null;
        }
        return requestedMenus;
    }

    @Override
    public List<Users> getUserProfiles() {
        TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u", Users.class);
        List<Users> allUsers = query.getResultList();
        if (allUsers.isEmpty()) {
            return null;
        }
        return allUsers;
    }

    @Override
    public Users checkIfUsernameExist(String username) {
        //This query will return an object of Users or null
        TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.username = :username", Users.class)
                .setParameter("username", username);
        List<Users> selectedUsers = query.getResultList();
        if (selectedUsers.isEmpty()) {
            return null;
        }
        return selectedUsers.get(0);
    }

    @Override
    public Branch getBranchUsingName(String branchName) {
        TypedQuery<Branch> query = em.createQuery("SELECT b FROM Branch b WHERE b.branchName = :branchName", Branch.class)
                .setParameter("branchName", branchName);
        List<Branch> selectedBranch = query.getResultList();
        if (selectedBranch.isEmpty()) {
            return null;
        }
        return selectedBranch.get(0);
    }

    @Override
    @Transactional
    public Users createUserProfile(Users user) {
        em.persist(user);
        em.flush();
        return user;
    }

    @Override
    @Transactional
    public Users dropUserProfile(Users user) {
        em.remove(em.contains(user) ? user : em.merge(user));
        em.flush();
        return user;
    }

    @Override
    public Users getUserProfileUsingId(Long id) {
        TypedQuery<Users> query = em.createQuery("SELECT u FROM Users u WHERE u.id = :id", Users.class)
                .setParameter("id", id);
        List<Users> selectedUsers = query.getResultList();
        if (selectedUsers.isEmpty()) {
            return null;
        }
        return selectedUsers.get(0);
    }

    @Override
    public List<UserRole> getRoleList(String appType) {
        TypedQuery<UserRole> query = em.createQuery("SELECT r FROM UserRole r WHERE r.landingPageId.appType = :appType", UserRole.class)
                .setParameter("appType", appType);
        List<UserRole> selectedRoles = query.getResultList();
        if (selectedRoles.isEmpty()) {
            return null;
        }
        return selectedRoles;
    }

    @Override
    public List<String> getRoles(String appType) {
        TypedQuery<String> query = em.createQuery("SELECT l.roleName FROM LandingPage l WHERE l.appType = :appType", String.class)
                .setParameter("appType", appType);
        List<String> selectedRoles = query.getResultList();
        if (selectedRoles.isEmpty()) {
            return null;
        }
        return selectedRoles;
    }

    @Override
    public List<String> getActiveUsers() {
        TypedQuery<String> query = em.createQuery("SELECT r.username FROM Users r WHERE r.status = 'Enabled'", String.class);
        List<String> selectedUsers = query.getResultList();
        if (selectedUsers.isEmpty()) {
            return null;
        }
        return selectedUsers;
    }

    @Override
    public List<String> getAllUsers() {
        TypedQuery<String> query = em.createQuery("SELECT r.username FROM Users r", String.class);
        List<String> selectedUsers = query.getResultList();
        if (selectedUsers.isEmpty()) {
            return null;
        }
        return selectedUsers;
    }

    @Override
    public LandingPage getLandingPageUsingRoleName(String roleName, String appType) {
        TypedQuery<LandingPage> query = em.createQuery("SELECT l FROM LandingPage l WHERE l.roleName = :roleName AND l.appType = :appType", LandingPage.class)
                .setParameter("roleName", roleName)
                .setParameter("appType", appType);
        List<LandingPage> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    @Transactional
    public UserRole createUserRole(UserRole userRole) {
        em.persist(userRole);
        em.flush();
        return userRole;
    }

    @Override
    public UserRole getUserRole(Users user, LandingPage landingPage) {
        TypedQuery<UserRole> query = em.createQuery("SELECT r FROM UserRole r WHERE r.usersId = :user AND r.landingPageId = :landingPage", UserRole.class)
                .setParameter("user", user)
                .setParameter("landingPage", landingPage);
        List<UserRole> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    @Transactional
    public UserRole updateUserRole(UserRole userRole) {
        em.merge(userRole);
        em.flush();
        return userRole;
    }

    @Override
    public UserRole getUserRoleUsingId(Long id) {
        TypedQuery<UserRole> query = em.createQuery("SELECT r FROM UserRole r WHERE r.id = :id", UserRole.class)
                .setParameter("id", id);
        List<UserRole> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    public String getUserLandingPageForAPI(Users principal, String appType) {
        TypedQuery<String> query = em.createQuery("SELECT l.roleName FROM LandingPage l JOIN UserRole u ON l.id = u.landingPageId WHERE l.appType = :appType AND u.usersId = :principal", String.class)
                .setParameter("appType", appType)
                .setParameter("principal", principal);
        List<String> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    public List<String> getUserMenusForAPI(Users principal, String appType) {
        TypedQuery<String> query = em.createQuery("SELECT u.menuRole FROM UserMenu u WHERE u.appType = :appType AND u.username = :principal", String.class)
                .setParameter("appType", appType)
                .setParameter("principal", principal);
        List<String> selectedMenus = query.getResultList();
        if (selectedMenus.isEmpty()) {
            return null;
        }
        return selectedMenus;
    }

    @Override
    public UserRole checkIfPrincipalHasRole(Users user, String appType) {
        TypedQuery<UserRole> query = em.createQuery("SELECT u FROM UserRole u JOIN LandingPage l ON u.landingPageId = l.id WHERE u.usersId = :user AND u.landingPageId = l.id AND l.appType = :appType", UserRole.class)
                .setParameter("user", user)
                .setParameter("appType", appType);
        List<UserRole> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    public List<UserMenu> getMenuList(String appType) {
        TypedQuery<UserMenu> query = em.createQuery("SELECT r FROM UserMenu r WHERE r.appType = :appType", UserMenu.class)
                .setParameter("appType", appType);
        List<UserMenu> selectedRoles = query.getResultList();
        if (selectedRoles.isEmpty()) {
            return null;
        }
        return selectedRoles;
    }

    @Override
    public List<String> getMenus(String appType) {
        TypedQuery<String> query = em.createQuery("SELECT l.roleName FROM AppRoles l WHERE l.appType = :appType", String.class)
                .setParameter("appType", appType);
        List<String> selectedRoles = query.getResultList();
        if (selectedRoles.isEmpty()) {
            return null;
        }
        return selectedRoles;
    }

    @Override
    public UserMenu getUserMenuUsingId(Long id) {
        TypedQuery<UserMenu> query = em.createQuery("SELECT m FROM UserMenu m WHERE m.id = :id", UserMenu.class)
                .setParameter("id", id);
        List<UserMenu> selectedUserMenu = query.getResultList();
        if (selectedUserMenu == null) {
            return null;
        }
        return selectedUserMenu.get(0);
    }

    @Override
    @Transactional
    public int dropUserMenu(UserMenu menu) {
        Query query = em.createQuery("DELETE FROM UserMenu u WHERE u.id = :id")
                .setParameter("id", menu.getId());
        int deletedMenu = query.executeUpdate();
        //em.remove(em.contains(menu) ? menu : em.merge(menu));
        //em.flush();
        return deletedMenu;
    }

    @Override
    @Transactional
    public UserMenu createUserMenu(UserMenu userMenu) {
        em.persist(userMenu);
        em.flush();
        return userMenu;
    }

    @Override
    public AppRoles getAppRolesUsingRoleName(String roleName, String appType) {
        TypedQuery<AppRoles> query = em.createQuery("SELECT l FROM AppRoles l WHERE l.roleName = :roleName AND l.appType = :appType", AppRoles.class)
                .setParameter("roleName", roleName)
                .setParameter("appType", appType);
        List<AppRoles> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    public UserMenu checkIfPrincipalHasMenu(Users user, String appType, String roleName) {
        TypedQuery<UserMenu> query = em.createQuery("SELECT u FROM UserMenu u JOIN AppRoles l ON u.appRolesId = l.id WHERE u.usersId = :user AND u.appRolesId = l.id AND l.appType = :appType AND l.roleName = :roleName", UserMenu.class)
                .setParameter("user", user)
                .setParameter("appType", appType)
                .setParameter("roleName", roleName);
        List<UserMenu> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    public String getBranchCode(String branchName) {
        TypedQuery<String> query = em.createQuery("SELECT b.branchCode FROM Branch b WHERE b.branchName = :branchName", String.class)
                .setParameter("branchName", branchName);
        List<String> selectedBranch = query.getResultList();
        if (selectedBranch.isEmpty()) {
            return null;
        }
        return selectedBranch.get(0);
    }

    @Override
    @Transactional
    public Users updateUser(Users userToPersist) {
        em.merge(userToPersist);
        em.flush();
        return userToPersist;
    }

    @Override
    public Collection<GroupRoles> getGroupRoles(String groupName) {
        TypedQuery<GroupRoles> query = em.createQuery("SELECT r FROM GroupRoles r WHERE r.roleGroup = :groupName", GroupRoles.class)
                .setParameter("groupName", groupName);
        List<GroupRoles> selectedGroup = query.getResultList();
        if (selectedGroup.isEmpty()) {
            return null;
        }
        return selectedGroup;
    }

    @Override
    public String getUserCurrentRoleGroup(String username, String appType) {
        TypedQuery<String> query = em.createQuery("SELECT r.menuRole FROM UserMenu r WHERE r.appType = :appType", String.class)
                .setParameter("appType", appType);
        List<String> selectedGroup = query.getResultList();
        if (selectedGroup.isEmpty()) {
            return null;
        }
        return selectedGroup.get(0);
    }

    @Override
    public Collection<GroupRoles> getRolesGroupedByAppType(String appType) {
        TypedQuery<GroupRoles> query = em.createQuery("SELECT r FROM GroupRoles r WHERE r.appType = :appType", GroupRoles.class)
                .setParameter("appType", appType);
        List<GroupRoles> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole;
    }

    @Override
    public Collection<RoleGroup> getRoleGroupsByAppType(String appType) {
        TypedQuery<RoleGroup> query = em.createQuery("SELECT r FROM RoleGroup r WHERE r.appType = :appType", RoleGroup.class)
                .setParameter("appType", appType);
        List<RoleGroup> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole;
    }

    @Override
    public Collection<RoleGroup> getRoleGroupsByAppType(String roleGroup, String appType) {
        TypedQuery<RoleGroup> query = em.createQuery("SELECT r FROM RoleGroup r WHERE r.appType = :appType AND r.groupName = :roleGroup", RoleGroup.class)
                .setParameter("appType", appType)
                .setParameter("roleGroup", roleGroup);
        List<RoleGroup> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole;
    }

    @Override
    public Collection<String> getRoleGroupsNameByAppType(String appType) {
        TypedQuery<String> query = em.createQuery("SELECT r.groupName FROM RoleGroup r WHERE r.appType = :appType", String.class)
                .setParameter("appType", appType);
        List<String> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole;
    }

    @Override
    public GroupRoles getGroupByGroupName(String groupName, String appType) {
        TypedQuery<GroupRoles> query = em.createQuery("SELECT r FROM GroupRoles r WHERE r.roleGroup = :groupName AND r.appType = :appType", GroupRoles.class)
                .setParameter("appType", appType)
                .setParameter("groupName", groupName);
        List<GroupRoles> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    @Transactional
    public GroupRoles dropGroupRole(GroupRoles group) {
        em.remove(em.contains(group) ? group : em.merge(group));
        em.flush();
        return group;
    }

    @Override
    @Transactional
    public RoleGroup dropRoleGroup(RoleGroup group) {
        em.remove(em.contains(group) ? group : em.merge(group));
        em.flush();
        return group;
    }

    @Override
    @Transactional
    public RoleGroup createRoleGroup(RoleGroup roleGroup) {
        em.persist(roleGroup);
        em.flush();
        return roleGroup;
    }

    @Override
    @Transactional
    public RoleGroup updateRoleGroup(RoleGroup roleGroup) {
        em.merge(roleGroup);
        em.flush();
        return roleGroup;
    }

    @Override
    public Collection<UserMenu> checkIfRoleGroupIsInUseByUser(String groupName) {
        TypedQuery<UserMenu> query = em.createQuery("SELECT r FROM UserMenu r WHERE r.menuRole = :groupName", UserMenu.class)
                .setParameter("groupName", groupName);
        List<UserMenu> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole;
    }

    @Override
    public RoleGroup getRoleGroupsByNameByAppType(String roleGroup, String appType) {
        TypedQuery<RoleGroup> query = em.createQuery("SELECT r FROM RoleGroup r WHERE r.appType = :appType AND r.groupName = :roleGroup", RoleGroup.class)
                .setParameter("roleGroup", roleGroup)
                .setParameter("appType", appType);
        List<RoleGroup> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    public GroupRoles checkIfRoleExistInGroup(String groupName, String roleName, String appType) {
        TypedQuery<GroupRoles> query = em.createQuery("SELECT r FROM GroupRoles r WHERE r.appType = :appType AND r.roleGroup = :groupName AND r.roleName = :roleName", GroupRoles.class)
                .setParameter("groupName", groupName)
                .setParameter("appType", appType)
                .setParameter("roleName", roleName);
        List<GroupRoles> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    @Transactional
    public GroupRoles createGroupRole(GroupRoles groupRole) {
        em.persist(groupRole);
        em.flush();
        return groupRole;
    }

    @Override
    public GroupRoles getGroupRolesUsignId(Long id) {
        TypedQuery<GroupRoles> query = em.createQuery("SELECT r FROM GroupRoles r WHERE r.id = :id", GroupRoles.class)
                .setParameter("id", id);
        List<GroupRoles> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    public String getRoleGroupOfUser(String principal, String appType) {
        TypedQuery<String> query = em.createQuery("SELECT r.menuRole FROM UserMenu r WHERE r.username = :username AND r.appType = :appType", String.class)
                .setParameter("username", principal)
                .setParameter("appType", appType);
        List<String> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    public List<String> getRolesForGroup(String groupName) {
        TypedQuery<String> query = em.createQuery("SELECT r.roleName FROM GroupRoles r WHERE r.roleGroup = :groupName", String.class)
                .setParameter("groupName", groupName);
        List<String> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole;
    }

    @Override
    public UserMenu getUserRoleGroupUsingUsername(String username, String appType) {
        TypedQuery<UserMenu> query = em.createQuery("SELECT r FROM UserMenu r WHERE r.username = :username AND r.appType = :appType", UserMenu.class)
                .setParameter("username", username)
                .setParameter("appType", appType);
        List<UserMenu> selectedRole = query.getResultList();
        if (selectedRole.isEmpty()) {
            return null;
        }
        return selectedRole.get(0);
    }

    @Override
    @Transactional
    public UserMenu updateUserRoleGroup(UserMenu roleGroup) {
        em.merge(roleGroup);
        em.flush();
        return roleGroup;
    }

    @Override
    @Transactional
    public UserTemp createUserTemp(UserTemp userTemp) {
        em.persist(userTemp);
        em.flush();
        return userTemp;
    }

    @Override
    @Transactional
    public UserTemp updateUserTemp(UserTemp userTemp) {
        em.merge(userTemp);
        em.flush();
        return userTemp;
    }

    @Override
    public UserTemp getUserTempUsingId(Long id) {
        TypedQuery<UserTemp> query = em.createQuery("SELECT r FROM UserTemp r WHERE r.id = :id", UserTemp.class)
                .setParameter("id", id);
        List<UserTemp> selectedTran = query.getResultList();
        if (selectedTran.isEmpty()) {
            return null;
        }
        return selectedTran.get(0);
    }

    @Override
    public Collection<UserTemp> getPendingProfileApproval() {
        TypedQuery<UserTemp> query = em.createQuery("SELECT r FROM UserTemp r", UserTemp.class);
        List<UserTemp> selectedTran = query.getResultList();
        if (selectedTran.isEmpty()) {
            return null;
        }
        return selectedTran;
    }

    @Override
    @Transactional
    public UserTemp dropUserTemp(UserTemp userTemp) {
        em.remove(em.contains(userTemp) ? userTemp : em.merge(userTemp));
        em.flush();
        return userTemp;
    }

    @Override
    public LocalDate getPasswordExpiryDate(String username) {
        TypedQuery<LocalDate> query = em.createQuery("SELECT r.passwordChangeDate FROM Users r WHERE r.username = :username", LocalDate.class)
                .setParameter("username", username);
        List<LocalDate> selectedTran = query.getResultList();
        if (selectedTran.isEmpty()) {
            return null;
        }
        return selectedTran.get(0);
    }

    @Override
    public ApprovalLevel getApprovalLevelUsingLevelCode(String levelCode) {
        TypedQuery<ApprovalLevel> query = em.createQuery("SELECT a FROM ApprovalLevel a WHERE a.levelCode = :levelCode", ApprovalLevel.class)
                .setParameter("levelCode", Integer.parseInt(levelCode));
        List<ApprovalLevel> selectedLevel = query.getResultList();
        if (selectedLevel.isEmpty()) {
            return null;
        }
        return selectedLevel.get(0);
    }

    @Override
    public Collection<ApprovalLevel> getApprovalLevel() {
        TypedQuery<ApprovalLevel> query = em.createQuery("SELECT a FROM ApprovalLevel a", ApprovalLevel.class);
        List<ApprovalLevel> selectedLevel = query.getResultList();
        if (selectedLevel.isEmpty()) {
            return null;
        }
        return selectedLevel;
    }

    @Override
    @Transactional
    public ApprovalLevel createApprovalLevel(ApprovalLevel approvalLevel) {
        em.persist(approvalLevel);
        em.flush();
        return approvalLevel;
    }

    @Override
    @Transactional
    public ApprovalLevel updateApprovalLevel(ApprovalLevel approvalLevel) {
        em.merge(approvalLevel);
        em.flush();
        return approvalLevel;
    }

    @Override
    public Collection<Promotion> getPromotionList() {
        TypedQuery<Promotion> query = em.createQuery("SELECT a FROM Promotion a ORDER BY a.department", Promotion.class);
        List<Promotion> promotion = query.getResultList();
        if (promotion.isEmpty()) {
            return null;
        }
        return promotion;
    }

    @Override
    public List<Users> getAuthorizersUsingBranch(String branchName) {
        String branch = "%" + branchName + "%";
        TypedQuery<Users> query = em.createQuery("SELECT a FROM Users a WHERE a.branch LIKE :branchName AND a.userType = 'Authorizer'", Users.class)
                .setParameter("branchName", branch);
        List<Users> users = query.getResultList();
        if (users.isEmpty()) {
            return null;
        }
        return users;
    }

}
