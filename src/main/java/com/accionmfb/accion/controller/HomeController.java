/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.controller;

import com.accionmfb.accion.payload.AuditDetailsPayload;
import com.accionmfb.accion.payload.AuditLogPayload;
import com.accionmfb.accion.payload.EnquiryPayload;
import com.accionmfb.accion.payload.LoginPayload;
import com.accionmfb.accion.payload.ParameterPayload;
import com.accionmfb.accion.payload.ResponsePayload;
import com.accionmfb.accion.payload.RolePayload;
import com.accionmfb.accion.payload.SingleFieldPayload;
import com.accionmfb.accion.payload.UserProfilePayload;
import com.accionmfb.accion.payload.UserRolePayload;
import com.accionmfb.accion.repository.UserRepository;
import com.accionmfb.accion.service.UserService;
import com.google.gson.Gson;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
@Controller
public class HomeController {

    @Autowired
    ServletContext servletContext;
    @Autowired
    MessageSource messageSource;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    UserDetailsService userDetailService;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private String alertMessage = "";
    static int loginTry = 0;
    Gson gson;
    RestTemplate restTemplate;
    LoginPayload loginPayloadForPasswordReset = new LoginPayload();

    HomeController() {
        gson = new Gson();
        restTemplate = new RestTemplate();
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("alertMessage", alertMessage);
        model.addAttribute("promotion", userService.getPromotionList());
        resetAlertMessage();
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("loginPayload", new LoginPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "login";
    }

    @PostMapping("/auth/login")
    public String login(@ModelAttribute("loginPayload") @Valid LoginPayload loginPayload, BindingResult bindingResult, Model model) throws Exception {
        String url = "http://127.0.0.1:8080/accion/api/service/auth/login";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.add("Authorization", userService.getAuthorizationHeader());
        header.add("Signature", userService.getSignatureHeader());
        String requestPayload = gson.toJson(loginPayload, LoginPayload.class);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestPayload, header);
        String result = restTemplate.postForObject(url, requestEntity, String.class);
        ResponsePayload response = gson.fromJson(result, ResponsePayload.class);

        if (response == null) {
            //Authentication did not go through
            alertMessage = messageSource.getMessage("appMessages.invalidLoginCredentials", new Object[0], Locale.ENGLISH);
            return "redirect:/login";
        }

        //Check if the default password is used
        if (response.getResponseCode().equals("94")) {
            alertMessage = response.getResponseMessage();
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loginPayload.getUsername(), loginPayload.getPassword(), null));
            return "redirect:/user-manager/default-password";
        }

        //Check the headers
        if (!"200".equals(response.getResponseCode())) {
            alertMessage = response.getResponseMessage();
            return "redirect:/login";
        }

        List<SimpleGrantedAuthority> newAuthorities = new ArrayList<>();
        if (response.getResponseCode().equals("200")) {
            //Retrieve all the user roles
            List<String> userRoles = userService.getUserRole(loginPayload.getUsername());
            if (userRoles != null) {
                for (String role : userRoles) {
                    newAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                }
            }
        }

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loginPayload.getUsername(), loginPayload.getPassword(), newAuthorities));
        //Set the online flag
        loginPayload.setUserOnlineStatus(true);
        String loginStatus = gson.toJson(loginPayload);
        userService.updateUserAsOnline(loginStatus);
        return "redirect:/user-manager/home";

    }

    @GetMapping("/user-manager/default-password/{userId}")
    public String defaultPassword(@PathVariable("userId") String userId, Model model) {
        loginPayloadForPasswordReset.setUsername(userId);
        model.addAttribute("loginPayload", loginPayloadForPasswordReset);
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "defaultpassword";
    }

    @GetMapping("/user-manager/default-password")
    public String defaultPassword(Model model, Principal principal) {
        loginPayloadForPasswordReset.setUsername(principal.getName());
        model.addAttribute("loginPayload", loginPayloadForPasswordReset);
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "defaultpassword";
    }

    @PostMapping("/user-manager/change-default-password")
    public String changeDefaultPassword(@ModelAttribute("loginPayload") @Valid LoginPayload loginPayload, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response, Model model, Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = userService.changeDefaultPassword(loginPayload);

        if (result.equals("Successful")) {
            Authentication auths = SecurityContextHolder.getContext().getAuthentication();
            if (auths != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            alertMessage = "Password change successful. Please re-login";
            return "redirect:/";
        }
        alertMessage = result;
        loginPayloadForPasswordReset.setUsername(loginPayload.getUsername());

        //Check if the request is coming from User Manager or other application
        if (principal == null) {
            //This  means its from other applications
            return "redirect:/user-manager/default-password/" + loginPayload.getUsername();
        }
        return "redirect:/user-manager/default-password";
    }

    @GetMapping("/user-manager/password-change")
    public String passwordChange(Model model) {
        model.addAttribute("loginPayload", new LoginPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "changepassword";
    }

    @PostMapping("/user-manager/change-password")
    public String changePassword(@ModelAttribute("loginPayload") @Valid LoginPayload loginPayload, BindingResult bindingResult, HttpServletRequest request, HttpServletResponse response, Model model, Principal principal) {
        String result = userService.changePassword(loginPayload, principal.getName());

        if (result.equals("Successful")) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
            alertMessage = "Password change successful. Please re-login";
            return "redirect:/login";
        }
        alertMessage = result;
        return "redirect:/user-manager/password-change";
    }

    @GetMapping("/user-manager/home")
    public String userManager(Model model) {
        model.addAttribute("auditLogPayload", new AuditLogPayload());
        model.addAttribute("enquiryPayload", new EnquiryPayload());
        int[] users = userService.getUserStatistics();
        model.addAttribute("totalUsers", users[0]);
        model.addAttribute("enabledUsers", users[1]);
        model.addAttribute("disableUsers", users[2]);
        model.addAttribute("totalInputer", users[3]);
        model.addAttribute("totalAuthorizer", users[4]);
        model.addAttribute("totalControl", users[5]);
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "usermanager";
    }

    @GetMapping("/user-manager/user-profile")
    @Secured("ROLE_USER_PROFILE")
    public String userProfile(Model model) {
        model.addAttribute("profilePayload", new UserProfilePayload());
        model.addAttribute("branches", userService.getBranches());
        model.addAttribute("userList", userService.getUserProfiles());
        model.addAttribute("enquiryPayload", new EnquiryPayload());
        model.addAttribute("approvalLevels", userService.getApprovalLevels());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "userprofile";
    }

    @GetMapping("/user-manager/user-audit-details/{id}")
    @ResponseBody
    public AuditDetailsPayload userAuditDetails(@PathVariable("id") String id) {
        return userService.getUserProfileUsingId(new Long(id));
    }

    @PostMapping("/user-manager/add-user-profile")
    public String addUserMenu(@ModelAttribute("profilePayload") @Valid UserProfilePayload profilePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.addUserProfile(profilePayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/user-profile";

    }

    @GetMapping("/user-manager/approval-level")
    @Secured("ROLE_")
    public String approvalLevel(Model model) {
        model.addAttribute("userProfilePayload", new UserProfilePayload());
        model.addAttribute("approvalLevels", userService.getApprovalLevels());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "approvallevel";
    }

    @PostMapping("/user-manager/update-approval-level")
    public String approvalLevel(@ModelAttribute("userProfilePayload") UserProfilePayload userProfilePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateApprovalLevel(userProfilePayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/approval-level";
    }

    @GetMapping("user-manager/maintain-user")
    @Secured("ROLE_USER_PROFILE")
    public String userMaintenance(Model model) {
        model.addAttribute("profilePayload", new UserProfilePayload());
        model.addAttribute("branches", userService.getBranches());
        model.addAttribute("approvalLevels", userService.getApprovalLevels());
        model.addAttribute("enquiryPayload", new EnquiryPayload());
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "usermaintenance";
    }

    @PostMapping("/user-manager/maintain-user-profile")
    public String maintainUserProfile(@ModelAttribute("userProfile") UserProfilePayload profilePayload, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateUserProfile(profilePayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/maintain-user";
    }

    @GetMapping("/user-manager/user-menu")
    @Secured("ROLE_USER_MENU")
    public String userMenu(Model model) {
        model.addAttribute("ussdRoleList", userService.getMenuList("Ussd"));
        model.addAttribute("bvnRoleList", userService.getMenuList("Bvn"));
        model.addAttribute("creditBureauRoleList", userService.getMenuList("Credit_Bureau"));
        model.addAttribute("a24RoleList", userService.getMenuList("A24"));
        model.addAttribute("amlRoleList", userService.getMenuList("Aml"));
        model.addAttribute("userRoleList", userService.getMenuList("User_Manager"));
        model.addAttribute("cardManagerRoleList", userService.getMenuList("Card_Manager"));
        model.addAttribute("serviceDeskRoleList", userService.getMenuList("Service_Desk"));

        model.addAttribute("ussdRoles", userService.getRoleGroups("Ussd"));
        model.addAttribute("bvnRoles", userService.getRoleGroups("Bvn"));
        model.addAttribute("creditBureauRoles", userService.getRoleGroups("Credit_Bureau"));
        model.addAttribute("a24Roles", userService.getRoleGroups("A24"));
        model.addAttribute("amlRoles", userService.getRoleGroups("Aml"));
        model.addAttribute("userRoles", userService.getRoleGroups("User_Manager"));
        model.addAttribute("cardManagerRoles", userService.getRoleGroups("Card_Manager"));
        model.addAttribute("serviceDeskRoles", userService.getRoleGroups("Service_Desk"));

        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "usermenu";
    }

    @GetMapping("/user-manager/a24/user-menu")
    @Secured("ROLE_USER_MENU")
    public String a24UserMenu(Model model) {
        model.addAttribute("a24RoleList", userService.getMenuList("A24"));
        model.addAttribute("a24Roles", userService.getRoleGroups("A24"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "a24usermenu";
    }

    @GetMapping("/user-manager/ussd/user-menu")
    @Secured("ROLE_USER_MENU")
    public String ussdUserMenu(Model model) {
        model.addAttribute("ussdRoleList", userService.getMenuList("Ussd"));
        model.addAttribute("ussdRoles", userService.getRoleGroups("Ussd"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "ussdusermenu";
    }

    @GetMapping("/user-manager/service-desk/user-menu")
    @Secured("ROLE_USER_MENU")
    public String serviceDeskUserMenu(Model model) {
        model.addAttribute("serviceDeskRoleList", userService.getMenuList("Service_Desk"));
        model.addAttribute("serviceDeskRoles", userService.getRoleGroups("Service_Desk"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "servicedeskusermenu";
    }

    @GetMapping("/user-manager/aml/user-menu")
    @Secured("ROLE_USER_MENU")
    public String amlUserMenu(Model model) {
        model.addAttribute("amlRoleList", userService.getMenuList("Aml"));
        model.addAttribute("amlRoles", userService.getRoleGroups("Aml"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "amlusermenu";
    }

    @GetMapping("/user-manager/credit-bureau/user-menu")
    @Secured("ROLE_USER_MENU")
    public String creditBureauUserMenu(Model model) {
        model.addAttribute("creditBureauRoleList", userService.getMenuList("Credit_Bureau"));
        model.addAttribute("creditBureauRoles", userService.getRoleGroups("Credit_Bureau"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "creditbureauusermenu";
    }

    @GetMapping("/user-manager/card-manager/user-menu")
    @Secured("ROLE_USER_MENU")
    public String cardManagerUserMenu(Model model) {
        model.addAttribute("cardManagerRoleList", userService.getMenuList("Card_Manager"));
        model.addAttribute("cardManagerRoles", userService.getRoleGroups("Card_Manager"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "cardmanagerusermenu";
    }

    @PostMapping("/user-manager/ussd/add-menu")
    public String addUSSDMenu(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateMenu(userRolePayload, principal.getName(), "Ussd");
        alertMessage = result;
        return "redirect:/user-manager/ussd/user-menu";
    }

    @PostMapping("/user-manager/credit-bureau/add-menu")
    public String addCreditBureauMenu(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateMenu(userRolePayload, principal.getName(), "Credit_Bureau");
        alertMessage = result;
        return "redirect:/user-manager/credit-bureau/user-menu";
    }

    @PostMapping("/user-manager/service-desk/add-menu")
    public String addServiceDeskMenu(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateMenu(userRolePayload, principal.getName(), "Service_Desk");
        alertMessage = result;
        return "redirect:/user-manager/service-desk/user-menu";
    }

    @PostMapping("/user-manager/a24/add-menu")
    public String addA24Menu(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateMenu(userRolePayload, principal.getName(), "A24");
        alertMessage = result;
        return "redirect:/user-manager/a24/user-menu";
    }

    @PostMapping("/user-manager/card-manager/add-menu")
    public String addCardManagerMenu(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateMenu(userRolePayload, principal.getName(), "Card_Manager");
        alertMessage = result;
        return "redirect:/user-manager/card-manager/user-menu";
    }

    @PostMapping("/user-manager/aml/add-menu")
    public String addAmlMenu(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateMenu(userRolePayload, principal.getName(), "Aml");
        alertMessage = result;
        return "redirect:/user-manager/aml/user-menu";
    }

    @PostMapping("/user-manager/add-user-manager-menu")
    public String addUserMenu(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateMenu(userRolePayload, principal.getName(), "User_Manager");
        alertMessage = result;
        return "redirect:/user-manager/user-menu";
    }

    @GetMapping("/user-manager/user-role")
    @Secured("ROLE_LANDING_PAGE")
    public String userRole(Model model) {
        model.addAttribute("userRoleList", userService.getRoleList("User_Manager"));
        model.addAttribute("userRoles", userService.getRoles("User_Manager"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "userrole";
    }

    @GetMapping("/user-manager/a24/user-role")
    @Secured("ROLE_LANDING_PAGE")
    public String a24UserRole(Model model) {
        model.addAttribute("a24RoleList", userService.getRoleList("A24"));
        model.addAttribute("a24Roles", userService.getRoles("A24"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "a24userrole";
    }

    @GetMapping("/user-manager/credit-bureau/user-role")
    @Secured("ROLE_LANDING_PAGE")
    public String creditBureauUserRole(Model model) {
        model.addAttribute("creditBureauRoleList", userService.getRoleList("Credit_Bureau"));
        model.addAttribute("creditBureauRoles", userService.getRoles("Credit_Bureau"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "creditbureauuserrole";
    }

    @GetMapping("/user-manager/ussd/user-role")
    @Secured("ROLE_LANDING_PAGE")
    public String ussdUserRole(Model model) {
        model.addAttribute("ussdRoleList", userService.getRoleList("Ussd"));
        model.addAttribute("ussdRoles", userService.getRoles("Ussd"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "ussduserrole";
    }

    @GetMapping("/user-manager/aml/user-role")
    @Secured("ROLE_LANDING_PAGE")
    public String amlUserRole(Model model) {
        model.addAttribute("amlRoleList", userService.getRoleList("Aml"));
        model.addAttribute("amlRoles", userService.getRoles("Aml"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "amluserrole";
    }

    @GetMapping("/user-manager/service-desk/user-role")
    @Secured("ROLE_LANDING_PAGE")
    public String serviceDeskUserRole(Model model) {
        model.addAttribute("serviceDeskRoleList", userService.getRoleList("Service_Desk"));
        model.addAttribute("serviceDeskRoles", userService.getRoles("Service_Desk"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "servicedeskuserrole";
    }

    @GetMapping("/user-manager/card-manager/user-role")
    @Secured("ROLE_LANDING_PAGE")
    public String cardManagerUserRole(Model model) {
        model.addAttribute("cardManagerRoleList", userService.getRoleList("Card_Manager"));
        model.addAttribute("cardManagerRoles", userService.getRoles("Card_Manager"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "cardmanageruserrole";
    }

    @PostMapping("/user-manager/ussd/update-role")
    public String addUSSDRole(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, @RequestParam(value = "action") String param, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateRole(userRolePayload, principal.getName(), "Ussd");
        alertMessage = result;
        return "redirect:/user-manager/ussd/user-role";
    }

    @PostMapping("/user-manager/credit-bureau/update-role")
    public String addCreditBureauRole(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, @RequestParam(value = "action") String param, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateRole(userRolePayload, principal.getName(), "Credit_Bureau");
        alertMessage = result;
        return "redirect:/user-manager/credit-bureau/user-role";
    }

    @PostMapping("/user-manager/aml/update-role")
    public String addAMLRole(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, @RequestParam(value = "action") String param, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateRole(userRolePayload, principal.getName(), "Aml");
        alertMessage = result;
        return "redirect:/user-manager/aml/user-role";
    }

    @PostMapping("/user-manager/service-desk/update-role")
    public String addServiceDeskRole(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, @RequestParam(value = "action") String param, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateRole(userRolePayload, principal.getName(), "Service_Desk");
        alertMessage = result;
        return "redirect:/user-manager/service-desk/user-role";
    }

    @PostMapping("/user-manager/a24/update-role")
    public String addA24Role(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, @RequestParam(value = "action") String param, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateRole(userRolePayload, principal.getName(), "A24");
        alertMessage = result;
        return "redirect:/user-manager/a24/user-role";
    }

    @PostMapping("/user-manager/card-manager/update-role")
    public String addCardManagerRole(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, @RequestParam(value = "action") String param, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateRole(userRolePayload, principal.getName(), "Card_Manager");
        alertMessage = result;
        return "redirect:/user-manager/card-manager/user-role";
    }

    @PostMapping("/user-manager/update-user-role")
    public String addUserRole(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, @RequestParam(value = "action") String param, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateRole(userRolePayload, principal.getName(), "User_Manager");
        alertMessage = result;
        return "redirect:/user-manager/user-role";
    }

    @GetMapping("/user-manager/system-parameter")
    @Secured("ROLE_SYSTEM_PARAMETER")
    public String systemParameter(Model model) {
        model.addAttribute("parameterPayload", new ParameterPayload());
        model.addAttribute("systemParameterList", userService.getSystemParameters());
        model.addAttribute("parameterNames", userService.getSystemParameterNames());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "systemparameter";
    }

    @PostMapping("/user-manager/update-system-parameter")
    public String updateSystemParameter(@ModelAttribute("parameterPayload") @Valid ParameterPayload parameterPayload, BindingResult bindingResult, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateSystemParameter(parameterPayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/system-parameter";

    }

    @GetMapping("/user-manager/audit-log")
    @Secured("ROLE_USER_MANAGER_AUDIT")
    public String getAuditLogs(Model model) {
        model.addAttribute("auditLogPayload", new AuditLogPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "auditlog";
    }

    @PostMapping("/auth/logout")
    public String logoutPage(HttpServletRequest request, HttpServletResponse response, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            //Update the user online status
            LoginPayload loginPayload = new LoginPayload();
            loginPayload.setUsername(auth.getName());
            loginPayload.setUserOnlineStatus(false);
            String loginStatus = gson.toJson(loginPayload);
            userService.updateUserAsOnline(loginStatus);
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        alertMessage = "Your session is terminated and you are logged out";
        return "redirect:/";
    }

    @GetMapping("/user-manager/group-roles")
    @Secured("ROLE_GROUP_ROLE")
    public String groupRoles(Model model) {
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("rolePayload", new RolePayload());
        model.addAttribute("userRoleGroups", userService.getRoleGroupsNameByAppType("User_Manager"));
        model.addAttribute("userAppRoles", userService.getMenus("User_Manager"));
        model.addAttribute("userRoleList", userService.getRolesGroupedByAppType("User_Manager"));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "grouproles";
    }

    @GetMapping("/user-manager/a24/group-roles")
    @Secured("ROLE_GROUP_ROLE")
    public String a24CoreGroupRoles(Model model) {
        //A24 Core 
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("rolePayload", new RolePayload());
        model.addAttribute("a24RoleGroups", userService.getRoleGroupsNameByAppType("A24"));
        model.addAttribute("a24AppRoles", userService.getMenus("A24"));
        model.addAttribute("a24RoleList", userService.getRolesGroupedByAppType("A24"));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "a24grouproles";
    }

    @GetMapping("/user-manager/ussd/group-roles")
    @Secured("ROLE_GROUP_ROLE")
    public String ussdGroupRoles(Model model) {
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("rolePayload", new RolePayload());
        model.addAttribute("ussdRoleGroups", userService.getRoleGroupsNameByAppType("Ussd"));
        model.addAttribute("ussdAppRoles", userService.getMenus("Ussd"));
        model.addAttribute("ussdRoleList", userService.getRolesGroupedByAppType("Ussd"));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "ussdgrouproles";
    }

    @GetMapping("/user-manager/credit-bureau/group-roles")
    @Secured("ROLE_GROUP_ROLE")
    public String creditBureauGroupRoles(Model model) {
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("rolePayload", new RolePayload());
        model.addAttribute("creditBureauRoleGroups", userService.getRoleGroupsNameByAppType("Credit_Bureau"));
        model.addAttribute("creditBureauAppRoles", userService.getMenus("Credit_Bureau"));
        model.addAttribute("creditBureauRoleList", userService.getRolesGroupedByAppType("Credit_Bureau"));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "creditbureaugrouproles";
    }

    @GetMapping("/user-manager/aml/group-roles")
    @Secured("ROLE_GROUP_ROLE")
    public String amlGroupRoles(Model model) {
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("rolePayload", new RolePayload());
        model.addAttribute("amlRoleGroups", userService.getRoleGroupsNameByAppType("Aml"));
        model.addAttribute("amlAppRoles", userService.getMenus("Aml"));
        model.addAttribute("amlRoleList", userService.getRolesGroupedByAppType("Aml"));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "amlgrouproles";
    }

    @PostMapping("/user-manager/add-user-role-group")
    public String roleGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.updateRoleGroup(rolePayload, principal.getName(), "User_Manager");
        alertMessage = result;
        return "redirect:/user-manager/group-roles";
    }

    @PostMapping("/user-manager/ussd/add-role-group")
    public String ussddRoleGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.updateRoleGroup(rolePayload, principal.getName(), "Ussd");
        alertMessage = result;
        return "redirect:/user-manager/ussd/group-roles";
    }

    @PostMapping("/user-manager/credit-bureau/add-role-group")
    public String creditBureauRoleGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.updateRoleGroup(rolePayload, principal.getName(), "Credit_Bureau");
        alertMessage = result;
        return "redirect:/user-manager/credit-bureau/group-roles";
    }

    @PostMapping("/user-manager/aml/add-role-group")
    public String amlRoleGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.updateRoleGroup(rolePayload, principal.getName(), "Aml");
        alertMessage = result;
        return "redirect:/user-manager/aml/group-roles";
    }

    @PostMapping("/user-manager/a24/add-role-group")
    public String a24RoleGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.updateRoleGroup(rolePayload, principal.getName(), "A24");
        alertMessage = result;
        return "redirect:/user-manager/a24/group-roles";
    }

    @PostMapping("/user-manager/add-user-role-to-group")
    public String addRoleToGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.addRoleToGroup(rolePayload, principal.getName(), "User_Manager");
        alertMessage = result;
        return "redirect:/user-manager/group-roles";
    }

    @PostMapping("/user-manager/ussd/add-role-to-group")
    public String addUssdRoleToGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.addRoleToGroup(rolePayload, principal.getName(), "Ussd");
        alertMessage = result;
        return "redirect:/user-manager/ussd/group-roles";
    }

    @PostMapping("/user-manager/a24/add-role-to-group")
    public String addA24RoleToGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.addRoleToGroup(rolePayload, principal.getName(), "A24");
        alertMessage = result;
        return "redirect:/user-manager/a24/group-roles";
    }

    @PostMapping("/user-manager/credit-bureau/add-role-to-group")
    public String addCreditBureauRoleToGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.addRoleToGroup(rolePayload, principal.getName(), "Credit_Bureau");
        alertMessage = result;
        return "redirect:/user-manager/credit-bureau/group-roles";
    }

    @PostMapping("/user-manager/aml/add-role-to-group")
    public String addAmlRoleToGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.addRoleToGroup(rolePayload, principal.getName(), "Aml");
        alertMessage = result;
        return "redirect:/user-manager/aml/group-roles";
    }

    @PostMapping("/user-manager/drop-group-role")
    public String dropGroupRole(@ModelAttribute("roleDropPayload") SingleFieldPayload roleDropPayload, Model model, Principal principal) {
        //Persis the entity
        String result = userService.dropRoleGroup(roleDropPayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/group-roles";
    }

    @PostMapping("/user-manager/ussd/drop-group-role")
    public String dropUssdGroupRole(@ModelAttribute("roleDropPayload") SingleFieldPayload roleDropPayload, Model model, Principal principal) {
        //Persis the entity
        String result = userService.dropRoleGroup(roleDropPayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/ussd/group-roles";
    }

    @PostMapping("/user-manager/a24/drop-group-role")
    public String dropA24GroupRole(@ModelAttribute("roleDropPayload") SingleFieldPayload roleDropPayload, Model model, Principal principal) {
        //Persis the entity
        String result = userService.dropRoleGroup(roleDropPayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/a24/group-roles";
    }

    @PostMapping("/user-manager/credit-bureau/drop-group-role")
    public String dropCreditBureauGroupRole(@ModelAttribute("roleDropPayload") SingleFieldPayload roleDropPayload, Model model, Principal principal) {
        //Persis the entity
        String result = userService.dropRoleGroup(roleDropPayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/credit-bureau/group-roles";
    }

    @PostMapping("/user-manager/aml/drop-group-role")
    public String dropAmlGroupRole(@ModelAttribute("roleDropPayload") SingleFieldPayload roleDropPayload, Model model, Principal principal) {
        //Persis the entity
        String result = userService.dropRoleGroup(roleDropPayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/aml/group-roles";
    }

    @GetMapping("/user-manager/pending-profile-approval")
    @Secured("ROLE_A_USER_PROFILE")
    public String profileApproval(Model model) {
        model.addAttribute("approvalList", userService.getPendingApproval());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "approveuserprofile";
    }

    @GetMapping("/user-manager/approve-profile-record/{id}")
    @Secured("ROLE_A_USER_PROFILE")
    public String approveUserProfile(@PathVariable String id, Model model, Principal principal) {
        //Check if the id is supplied
        if (0 >= new Integer(id)) {
            alertMessage = messageSource.getMessage("appMessages.invalidRecordId", new Object[0], Locale.ENGLISH);
            return "redirect:/user-manager/pending-profile-approval";
        }
        //Persis the entity
        String result = userService.approveUserProfile(new Long(id), principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/pending-profile-approval";
    }

    @GetMapping("/user-manager/drop-profile-approval/{id}")
    @Secured("ROLE_A_USER_PROFILE")
    public String dropUserProfileApproval(@PathVariable String id, Model model, Principal principal) {
        //Check if the id is supplied
        if (0 >= new Integer(id)) {
            alertMessage = messageSource.getMessage("appMessages.invalidRecordId", new Object[0], Locale.ENGLISH);
            return "redirect:/user-manager/pending-profile-approval";
        }
        //Persis the entity
        String result = userService.dropUserProfile(new Long(id), principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/pending-profile-approval";
    }

    @GetMapping("/user-manager/password-encryption")
    public String passwordEncryption(Model model) {
        model.addAttribute("loginPayload", new LoginPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "passwordencryption";
    }

    @PostMapping("/user-manager/encrypt-password")
    public String passwordEncryption(@ModelAttribute("loginPayload") LoginPayload loginPayload, Principal principal) {
        //Persis the entity
        String result = userService.encryptPassword(loginPayload, principal.getName());
        alertMessage = result;
        return "redirect:/user-manager/password-encryption";
    }

    @GetMapping("/password-try/{username}")
    @ResponseBody
    public String getUserPasswordTry(@PathVariable("username") String username) {
        return userService.getUserPasswordTry(username);
    }

    @GetMapping("/user-manager/ais-users")
    @Secured("ROLE_USER_REPORT")
    public String userProfileList(Model model) {
        model.addAttribute("profilePayload", new UserProfilePayload());
        model.addAttribute("branches", userService.getBranches());
        model.addAttribute("userList", userService.getUserProfiles());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "userprofilereport";
    }

    @GetMapping("/user-manager/alen/user-role")
    @Secured("ROLE_LANDING_PAGE")
    public String alenUserRole(Model model) {
        model.addAttribute("alenRoleList", userService.getRoleList("ALEN"));
        model.addAttribute("alenRoles", userService.getRoles("ALEN"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "alenuserrole";
    }

    @PostMapping("/user-manager/alen/update-role")
    public String addALENRole(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, @RequestParam(value = "action") String param, Model model, Principal principal) {
        //This is an update. Call service to update the entity
        String result = userService.updateRole(userRolePayload, principal.getName(), "ALEN");
        alertMessage = result;
        return "redirect:/user-manager/alen/user-role";
    }

    @GetMapping("/user-manager/alen/group-roles")
    @Secured("ROLE_GROUP_ROLE")
    public String alenGroupRoles(Model model) {
        //A24 Core 
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("rolePayload", new RolePayload());
        model.addAttribute("alenRoleGroups", userService.getRoleGroupsNameByAppType("ALEN"));
        model.addAttribute("alenAppRoles", userService.getMenus("ALEN"));
        model.addAttribute("alenRoleList", userService.getRolesGroupedByAppType("ALEN"));
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "alengrouproles";
    }

    @PostMapping("/user-manager/alen/add-role-group")
    public String alenRoleGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.updateRoleGroup(rolePayload, principal.getName(), "ALEN");
        alertMessage = result;
        return "redirect:/user-manager/alen/group-roles";
    }

    @GetMapping("/user-manager/alen/user-menu")
    @Secured("ROLE_USER_MENU")
    public String alenUserMenu(Model model) {
        model.addAttribute("alenRoleList", userService.getMenuList("ALEN"));
        model.addAttribute("alenRoles", userService.getRoleGroups("ALEN"));
        model.addAttribute("users", userService.getActiveUsers());
        model.addAttribute("userRolePayload", new UserRolePayload());
        model.addAttribute("roleDropPayload", new SingleFieldPayload());
        model.addAttribute("alertMessage", alertMessage);
        resetAlertMessage();
        return "alenusermenu";
    }

    @PostMapping("/user-manager/alen/add-menu")
    public String addALENMenu(@ModelAttribute("userRolePayload") @Valid UserRolePayload userRolePayload, Model model, Principal principal) {
        //This is a new user role. Persist entity and retrieve the message
        String result = userService.updateMenu(userRolePayload, principal.getName(), "ALEN");
        alertMessage = result;
        return "redirect:/user-manager/alen/user-menu";
    }

    @PostMapping("/user-manager/alen/add-role-to-group")
    public String addALENRoleToGroup(@ModelAttribute("rolePayload") @Valid RolePayload rolePayload, Model model, Principal principal) {
        String result = userService.addRoleToGroup(rolePayload, principal.getName(), "ALEN");
        alertMessage = result;
        return "redirect:/user-manager/alen/group-roles";
    }

    private void resetAlertMessage() {
        alertMessage = "";
    }

}
