/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.service;

import com.accionmfb.accion.model.ApprovalLevel;
import com.accionmfb.accion.model.AuditLog;
import com.accionmfb.accion.model.GroupRoles;
import com.accionmfb.accion.model.LandingPage;
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
import com.accionmfb.accion.payload.ResponsePayload;
import com.accionmfb.accion.payload.RolePayload;
import com.accionmfb.accion.payload.SingleFieldPayload;
import com.accionmfb.accion.payload.UserProfilePayload;
import com.accionmfb.accion.payload.UserRoleAPIPayload;
import com.accionmfb.accion.payload.UserRolePayload;
import com.accionmfb.accion.repository.UserRepository;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    AccionService accionService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MessageSource messageSource;
    @Autowired
    ServletContext servletContext;
    @Autowired
    private UserDetailsServiceImpl userDetailService;
    @Autowired
    Environment env;
    private static final String BASE_URL = "/api/service";
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private final BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder();
    private static final Integer THREE_SECONDS = 3000;
    String computerHostName = "";
    String serviceState = "";
    Gson gson;
    static int loginTry = 0;
    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    private Pattern pattern;
    private Pattern controlPattern;
    private Matcher matcher;
    private Matcher controlMatcher;

    private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
    private static final String CONTROL_PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{1,20})";

    UserServiceImpl() {
        this.gson = new Gson();
        pattern = Pattern.compile(PASSWORD_PATTERN);
        controlPattern = Pattern.compile(CONTROL_PASSWORD_PATTERN);
        try {
            computerHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
        }
    }

    @Override
    public String loginClient(String requestPayload) {
        LoginPayload loginPayload = new LoginPayload();
        ResponsePayload responsePayload = new ResponsePayload();
        loginPayload = gson.fromJson(requestPayload, LoginPayload.class);
        if (loginPayload.getUsername() == null || loginPayload.getPassword() == null) {
            //ResponsePayload responsePayload = new ResponsePayload();
            responsePayload.setResponseCode("415");
            responsePayload.setResponseMessage("Required filed(s) missing in the request");
            //Reset the loginRetry counter
            loginTry = 0;
            return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Check if anonymous 
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Retrieve the User details from the DB
        UserDetails auth = userDetailService.loadUserByUsername(loginPayload.getUsername());
        if (auth == null) {
            //ResponsePayload responsePayload = new ResponsePayload();
            responsePayload.setResponseCode("415");
            responsePayload.setResponseMessage("Login account not found. Contact your administrator");
            //Reset the loginRetry counter
            loginTry = 0;
            java.util.logging.Logger.getLogger(UserServiceImpl.class.getName()).log(Level.INFO, "Use Login :".concat(loginPayload.getUsername()), loginTry);
            return gson.toJson(responsePayload, ResponsePayload.class);

        }

        //Check if the user principal is anonymous with anonymous role
        if (!auth.isAccountNonLocked() || !auth.isEnabled()) {
            //ResponsePayload responsePayload = new ResponsePayload();
            responsePayload.setResponseCode("415");
            responsePayload.setResponseMessage("Account locked. Please contact the administrator");
            //Reset the loginRetry counter
            loginTry = 0;
            return gson.toJson(responsePayload, ResponsePayload.class);

        }

        //Check if the username and password is a match
        Boolean passwordMatch = bCryptEncoder.matches(loginPayload.getPassword(), auth.getPassword());
        Users user = userRepository.getUserUsingUsername(loginPayload.getUsername());
        if (passwordMatch) {
            if (getUserResetTime(loginPayload.getUsername()).isAfter(LocalDateTime.now())) {
                //ResponsePayload responsePayload = new ResponsePayload();
                responsePayload.setResponseCode("415");
                responsePayload.setResponseMessage("Reset time has not elapse. Please wait");
                //Reset the loginRetry counter
                loginTry = 0;
                return gson.toJson(responsePayload, ResponsePayload.class);
            }
            //Update user password try

            user.setPasswordTry(0);
            Users updatedUser = userRepository.updateUser(user);

        }

        if (!passwordMatch) {
            Users updatedUser = new Users();
            if (user.getPasswordTry() == 0) {
                //Update user password try
                user.setPasswordTry(loginTry + 1);
                updatedUser = userRepository.updateUser(user);
            } else {
                //Update user password try
                user.setPasswordTry(user.getPasswordTry() + 1);
                updatedUser = userRepository.updateUser(user);
            }
            java.util.logging.Logger.getLogger(UserServiceImpl.class.getName()).log(Level.INFO, "Login Count [{0}] ", loginPayload.getUsername() + " " + user.getPasswordTry());
            //Get the password retry count
            String passwordRetryCount = accionService.getSystemParameter("Password_Retry_Count");
            int retryCount = 0;
            try {
                retryCount = Integer.parseInt(passwordRetryCount);

            } catch (NumberFormatException nfe) {
                retryCount = 3;
            }

            //Check if the maximum login try is reached
            if (updatedUser.getPasswordTry() == retryCount) {
                java.util.logging.Logger.getLogger(UserServiceImpl.class.getName()).log(Level.INFO, "Maximum Login try reached [{0}] ", loginPayload.getUsername() + " " + updatedUser.getPasswordTry());
                //Lock the user for some time
                lockUser(loginPayload.getUsername());
                responsePayload.setResponseCode("415");
                responsePayload.setResponseMessage("Your account is locked due to multiple login attempt");
                loginTry = 0;
                user.setPasswordTry(0);
                user.setStatus("Disabled");
                updatedUser = userRepository.updateUser(user);
                java.util.logging.Logger.getLogger(UserServiceImpl.class.getName()).log(Level.INFO, "Account Locked [{0}] ", loginPayload.getUsername());
                return gson.toJson(responsePayload, ResponsePayload.class);

            }
            responsePayload.setResponseCode("415");
            responsePayload.setResponseMessage("Invalid login credentials. Username or Password incorrect");

            return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Check if the user needs to change password
        if (LocalDate.now().isAfter(userRepository.getPasswordExpiryDate(auth.getUsername()))) {
            responsePayload.setResponseCode("94");
            responsePayload.setResponseMessage("Current password has expired. Please change password");
            return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //ResponsePayload responsePayload = new ResponsePayload();
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage("Login successful");
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @Override
    public void createAuditLog(AuditLog auditLog) {
        userRepository.createAuditLog(auditLog);
    }

    @Override
    public List<String> getUserRole(String principal) {
        //Get the user role group
        String userRoleGroup = userRepository.getRoleGroupOfUser(principal, "User_Manager");
        //Retrieve the roles beloging to the group
        if (userRoleGroup == null) {
            return null;
        }
        List<String> roles = userRepository.getRolesForGroup(userRoleGroup);
        return roles;
    }

    @Override
    public String addUserProfile(UserProfilePayload profilePayload, String principal) {
        //Check if the username exist already
        Users userExist = userRepository.checkIfUsernameExist(profilePayload.getUsername());
        if (userExist != null) {
            return messageSource.getMessage("appMessages.usernameExist", new Object[0], Locale.ENGLISH);
        }

        //Check if the email is of Accion domain
        String[] emailSplit = profilePayload.getEmail().split("\\@");
        if (emailSplit.length == 2) {
            if (!emailSplit[1].equalsIgnoreCase("accionmfb.com")) {
                return messageSource.getMessage("appMessages.invalidAccionEmail", new Object[0], Locale.ENGLISH);
            }
        } else {
            return messageSource.getMessage("appMessages.invalidEmail", new Object[0], Locale.ENGLISH);
        }

        Users newUser = new Users();
        newUser.setUserType(profilePayload.getUserType());
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setCreatedBy(principal);
        newUser.setStatus("Pending");
        newUser.setLastLogin(LocalDate.now());
        newUser.setLastName(profilePayload.getLastName());
        newUser.setApprovalLevel(new Integer(profilePayload.getApprovalLevel()));
        newUser.setOtherName(profilePayload.getOtherName());
//        newUser.setAccountNumber(profilePayload.getAccountNumber());

        //Generate random password 
        RandomPasswordGenerator randomPassword = new RandomPasswordGenerator();
        String generatedPassword = randomPassword.generateRandomPassword();
        newUser.setPassword(bCryptEncoder.encode(generatedPassword));
        newUser.setDefaultPassword(generatedPassword);
        newUser.setResetTime(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        newUser.setUpdatedBy("");
        newUser.setUsername(profilePayload.getUsername());
        newUser.setEmail(profilePayload.getEmail());
        newUser.setPasswordTry(0);

        //Check if the more branches are selected
        if (profilePayload.getBranch() != null) {
            StringBuilder builder = new StringBuilder();
            for (String br : profilePayload.getBranch()) {
                builder.append(br.trim());
                builder.append(",");
            }
            newUser.setBranch(builder.toString());
        }

        Users createdUser = userRepository.createUserProfile(newUser);
        if (createdUser == null) {
            return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
        }

        //Add entry into the temp table
        UserTemp newRecord = new UserTemp();
        newRecord.setCreatedAt(LocalDate.now());
        newRecord.setCreatedBy(principal);
        newRecord.setInitialValue(createdUser.getId().toString());
        newRecord.setNewValue("");
        newRecord.setTransType("Create Profile");
        newRecord.setUserId(profilePayload.getUsername());
        UserTemp record = userRepository.createUserTemp(newRecord);
        return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
    }

    @Override
    public String updateUserProfile(UserProfilePayload profilePayload, String principal) {
        //Get the selected user
        Users selectedUser = userRepository.getUserUsingUsername(profilePayload.getUsername());
        if (selectedUser == null) {
            return messageSource.getMessage("appMessages.invalidUsername", new Object[0], Locale.ENGLISH);
        }

        if (profilePayload.getMaintenanceType().equals("Disable")) {
            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(selectedUser.getStatus());
            newRecord.setNewValue("Disabled");
            newRecord.setTransType("Disable Profile");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        if (profilePayload.getMaintenanceType().equals("Enable")) {
            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(selectedUser.getStatus());
            newRecord.setNewValue("Enabled");
            newRecord.setTransType("Enable Profile");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        if (profilePayload.getMaintenanceType().equals("ApprovalLevel")) {
            //Check if the new approval level is supplied
            if (profilePayload.getApprovalLevel() == null) {
                return messageSource.getMessage("appMessages.noRoleSelected", new Object[0], Locale.ENGLISH);
            }

            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(Integer.toString(selectedUser.getApprovalLevel()));
            newRecord.setNewValue(profilePayload.getApprovalLevel());
            newRecord.setTransType("Approval Level");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        if (profilePayload.getMaintenanceType().equals("LastName")) {
            if (profilePayload.getNewValue().equals("")) {
                return messageSource.getMessage("appMessages.missingNewValue", new Object[0], Locale.ENGLISH);
            }

            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(selectedUser.getLastName());
            newRecord.setNewValue(profilePayload.getNewValue());
            newRecord.setTransType("Last Name");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);

        }

        if (profilePayload.getMaintenanceType().equals("OtherName")) {
            if (profilePayload.getNewValue().equals("")) {
                return messageSource.getMessage("appMessages.missingNewValue", new Object[0], Locale.ENGLISH);
            }

            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(selectedUser.getOtherName());
            newRecord.setNewValue(profilePayload.getNewValue());
            newRecord.setTransType("Other Name");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        if (profilePayload.getMaintenanceType().equals("LoginReset")) {
            selectedUser.setResetTime(LocalDateTime.now());
            Users updatedUser = userRepository.updateUser(selectedUser);
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". Login timeout reset", "Update", principal, "Profile", principal);
            return messageSource.getMessage("appMessages.recordUpdatedSucceed", new Object[0], Locale.ENGLISH);
        }

        if (profilePayload.getMaintenanceType().equals("UserType")) {
            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(selectedUser.getUserType());
            newRecord.setNewValue(profilePayload.getNewValue());
            newRecord.setTransType("User Type");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        if (profilePayload.getMaintenanceType().equals("Branch")) {
            if (profilePayload.getBranch() == null) {
                return messageSource.getMessage("appMessages.noRoleSelected", new Object[0], Locale.ENGLISH);
            }
            StringBuilder builder = new StringBuilder();
            for (String br : profilePayload.getBranch()) {
                builder.append(br.trim());
                builder.append(",");
            }

            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(selectedUser.getBranch());
            newRecord.setNewValue(builder.toString());
            newRecord.setTransType("Branch");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        if (profilePayload.getMaintenanceType().equals("Email")) {
            if (profilePayload.getNewValue().equals("")) {
                return messageSource.getMessage("appMessages.missingNewValue", new Object[0], Locale.ENGLISH);
            }

            //Check if the email is of Accion domain
            String[] emailSplit = profilePayload.getNewValue().split("\\@");
            if (emailSplit.length == 2) {
                if (!emailSplit[1].equalsIgnoreCase("accionmfb.com")) {
                    return messageSource.getMessage("appMessages.invalidAccionEmail", new Object[0], Locale.ENGLISH);
                }
            } else {
                return messageSource.getMessage("appMessages.invalidEmail", new Object[0], Locale.ENGLISH);
            }

            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(selectedUser.getEmail());
            newRecord.setNewValue(profilePayload.getNewValue());
            newRecord.setTransType("Email");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);

        }

        if (profilePayload.getMaintenanceType().equals("Password")) {

            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(selectedUser.getDefaultPassword());
            RandomPasswordGenerator randomPassword = new RandomPasswordGenerator();
            String generatedPassword = randomPassword.generateRandomPassword();
            String newDefaultPassword = generatedPassword;
            newRecord.setNewValue(newDefaultPassword);
            newRecord.setTransType("Password Reset");
            newRecord.setUserId(profilePayload.getUsername());
            newRecord.setAppType("User Manager");
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);

        }

        return messageSource.getMessage("appMessages.recordUpdateFailed", new Object[0], Locale.ENGLISH);
    }

    @Override
    public String getSystemParameter(String paramName) {
        return userRepository.getParameter(paramName);
    }

    @Override
    public List<SystemParameters> getSystemParameters() {
        return userRepository.getSystemParameters();
    }

    @Override
    public List<String> getSystemParameterNames() {
        return userRepository.getSystemParameterNames();
    }

    @Override
    public String updateSystemParameter(ParameterPayload parameterPayload, String principal) {
        SystemParameters param = userRepository.getSystemParameterUsingName(parameterPayload.getParameter());
        if (param == null) {
            return messageSource.getMessage("appMessages.recordNotFound", new Object[0], Locale.ENGLISH);
        }

        String oldParameter = param.getParamValue();
        param.setParamValue(parameterPayload.getValue());
        SystemParameters updatedParameter = userRepository.updateParameter(param);
        if (updatedParameter == null) {
            return messageSource.getMessage("appMessages.recordUpdateFailed", new Object[0], Locale.ENGLISH);
        }

        //Create the audit log entries
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditClass("Parameter");
        auditLog.setActionDescription("System parameter changed for " + parameterPayload.getParameter() + " from " + oldParameter + " to " + parameterPayload.getValue());
        auditLog.setAuditType("Update");
        auditLog.setCreatedBy(principal);
        auditLog.setSystemDate(LocalDateTime.now());
        auditLog.setComputerName(computerHostName);
        auditLog.setDateCreated(LocalDateTime.now());
        auditLog.setAppType("Parameter");
        auditLog.setApprovedBy("System");
        auditLog.setDateApproved(LocalDateTime.now());
        createAuditLog(auditLog);
        return messageSource.getMessage("appMessages.recordUpdatedSucceed", new Object[0], Locale.ENGLISH);
    }

    @Override
    public List<AuditLog> getAuditLogs(AuditLogPayload auditLogPayload) {
        return null;
    }

    @Override
    public String getUserBranch(String principal) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getBranches() {
        return userRepository.getBranches();
    }

    @Override
    public String getCompanyCode(String branchName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getBranchUsingAccountNumber(String accountNumber) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPrincipalState(String branch) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getStateCodeUsingBranchName(String branchName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void lockUser(String username) {
        //Update the user role status = 'Disabled'
        Users user = userRepository.getUserUsingUsername(username);
        user.setStatus("Disabled");
        user.setResetTime(LocalDateTime.now().plusMinutes(new Long(getSystemParameter("Reset_Time"))));
        Users updatedUser = userRepository.updateUser(user);
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditClass("Profile");
        auditLog.setActionDescription("Profile for " + username + " locked due to multiple login attempts ");
        auditLog.setAuditType("Update");
        auditLog.setCreatedBy("System");
        auditLog.setSystemDate(LocalDateTime.now());
        auditLog.setComputerName(computerHostName);
        auditLog.setDateCreated(LocalDateTime.now());
        auditLog.setAppType("Profile");
        auditLog.setApprovedBy("System");
        auditLog.setDateApproved(LocalDateTime.now());
        createAuditLog(auditLog);
    }

    @Override
    public LocalDateTime getUserResetTime(String username) {
        return userRepository.getUserResetTime(username);
    }

    @Override
    public List<Users> getUserProfiles() {
        return userRepository.getUserProfiles();
    }

    @Override
    public String getAuthorizationHeader() {
        String AUTHORIZATION = "Basic " + Base64.getEncoder().encodeToString((env.getProperty("accion.authorization.username").trim() + ":" + env.getProperty("accion.authorization.password").trim()).getBytes());
        return AUTHORIZATION;
    }

    @Override
    public String getSignatureHeader() {
        String TIMESTAMP = TIMESTAMP_FORMAT.format(new Date());
        String SignaturePlain = String.format("%s:%s:%s", TIMESTAMP, env.getProperty("accion.authorization.username").trim(), env.getProperty("accion.authorization.secret.key").trim());
        String SIGNATURE = SignaturePlain; //this must be computation of SignaturePlain using SHA512
        SIGNATURE = hash(SignaturePlain, env.getProperty("accion.authorization.hash.key").trim());
        return SIGNATURE;
    }

    private static String hash(String plainText, String algorithm) {
        StringBuilder hexString = new StringBuilder();
        if (algorithm.equals("SHA512")) {
            algorithm = "SHA-512";
        }
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(plainText.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            System.out.println("Hex format : " + sb.toString());

            //convert the byte to hex format method 2
            for (int i = 0; i < byteData.length; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return hexString.toString().toUpperCase();
    }

    @Override
    public AuditDetailsPayload getUserProfileUsingId(Long id) {
        //This returns audit details for view
        Users selectedUser = userRepository.getUserProfileUsingId(id);
        AuditDetailsPayload newUserDetails = new AuditDetailsPayload();

        newUserDetails.setApprovalLevel(Integer.toString(selectedUser.getApprovalLevel()));
        newUserDetails.setResetTime(selectedUser.getResetTime().toString());
        newUserDetails.setLastLogin(selectedUser.getLastLogin().toString());
        newUserDetails.setCreatedBy(selectedUser.getCreatedBy());
        newUserDetails.setCreatedAt(selectedUser.getCreatedAt().toString());
        newUserDetails.setUpdatedBy(selectedUser.getUpdatedBy());
        newUserDetails.setUpdatedAt(selectedUser.getUpdatedAt().toString());
        newUserDetails.setDefaultPassword(selectedUser.getDefaultPassword());
        return newUserDetails;
    }

    @Override
    public String updateRoleStatus(Long id) {
        UserRole selectedUserRole = userRepository.getUserRoleUsingId(id);
        if (selectedUserRole == null) {
            return messageSource.getMessage("appMessages.recordNotFound", new Object[0], Locale.ENGLISH);
        }

        String currentStatus = "Enabled";
        if (selectedUserRole.getStatus().equals(currentStatus)) {
            currentStatus = "Disabled";
        } else {
            currentStatus = "Enabled";
        }
        selectedUserRole.setStatus(currentStatus);
        UserRole updatedRole = userRepository.updateUserRole(selectedUserRole);
        if (updatedRole == null) {
            return messageSource.getMessage("appMessages.recordUpdateFailed", new Object[0], Locale.ENGLISH);
        }
        return messageSource.getMessage("appMessages.recordUpdatedSucceed", new Object[0], Locale.ENGLISH);
    }

    @Override
    public String updateRole(UserRolePayload userRole, String principal, String appType) {
        Users selectedUser = userRepository.getUserUsingUsername(userRole.getUsername());
        LandingPage landingPage = userRepository.getLandingPageUsingRoleName(userRole.getUserRole(), appType);

        UserRole selectedUserRole = userRepository.checkIfPrincipalHasRole(selectedUser, appType);
        if (selectedUserRole == null) {
            //This means the user does not have such role. Create a new one

            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue(landingPage.getRoleName());
            newRecord.setNewValue(userRole.getUserRole());
            newRecord.setTransType("New Landing Page");
            newRecord.setUserId(selectedUser.getUsername());
            newRecord.setAppType(appType);
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);

        }

        UserTemp newRecord = new UserTemp();
        newRecord.setCreatedAt(LocalDate.now());
        newRecord.setCreatedBy(principal);
        newRecord.setInitialValue(selectedUserRole.getLandingPageId().getRoleName());
        newRecord.setNewValue(userRole.getUserRole());
        newRecord.setTransType("Update Landing Page");
        newRecord.setUserId(selectedUser.getUsername());
        newRecord.setAppType(appType);
        UserTemp record = userRepository.createUserTemp(newRecord);
        if (record == null) {
            return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
        }
        return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
    }

    @Override
    public String updateMenu(UserRolePayload userMenuPayload, String principal, String appType) {
        //Check if the user has a menu already
        Collection<UserMenu> currentMenus = userRepository.getUserMenus(userMenuPayload.getUsername(), appType);
        if (currentMenus == null) {

            //Check if a role is selected
            if ("".equals(userMenuPayload.getUserRole())) {
                return messageSource.getMessage("appMessages.noRoleSelected", new Object[0], Locale.ENGLISH);
            }
            //This is a new record
            UserTemp newRecord = new UserTemp();
            newRecord.setCreatedAt(LocalDate.now());
            newRecord.setCreatedBy(principal);
            newRecord.setInitialValue("");
            newRecord.setNewValue(userMenuPayload.getUserRole());
            newRecord.setTransType("New User Role");
            newRecord.setUserId(userMenuPayload.getUsername());
            newRecord.setAppType(appType);
            UserTemp record = userRepository.createUserTemp(newRecord);
            if (record == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //This is an update of record
        UserMenu selectedMenu = userRepository.getUserRoleGroupUsingUsername(userMenuPayload.getUsername(), appType);
        String role = "";
        //Check if a role is selected
        if ("".equals(userMenuPayload.getUserRole())) {
            role = "No Role";
        } else if (!userMenuPayload.getUserRole().equals("")) {
            role = userMenuPayload.getUserRole();
        }

        UserTemp newRecord = new UserTemp();
        newRecord.setCreatedAt(LocalDate.now());
        newRecord.setCreatedBy(principal);
        newRecord.setInitialValue(selectedMenu.getMenuRole());
        newRecord.setNewValue(role);
        newRecord.setTransType("Update User Role");
        newRecord.setUserId(userMenuPayload.getUsername());
        newRecord.setAppType(appType);
        UserTemp record = userRepository.createUserTemp(newRecord);
        if (record == null) {
            return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
        }
        return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
    }

    @Override
    public List<UserRole> getRoleList(String appType) {
        return userRepository.getRoleList(appType);
    }

    @Override
    public List<String> getRoles(String appType) {
        return userRepository.getRoles(appType);
    }

    @Override
    public List<UserMenu> getMenuList(String appType) {
        return userRepository.getMenuList(appType);
    }

    @Override
    public List<String> getMenus(String appType) {
        return userRepository.getMenus(appType);
    }

    @Override
    public List<String> getActiveUsers() {
        return userRepository.getActiveUsers();
    }

    @Override
    public List<String> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public String getUserTypeForAPI(String requestPayload) {
        LoginPayload loginPayload = new LoginPayload();
        loginPayload = gson.fromJson(requestPayload, LoginPayload.class);
        Users user = userRepository.getUserUsingUsername(loginPayload.getUsername());

        return user.getUserType();
    }

    @Override
    public String getUserLandingPageForAPI(String requestPayload) {
        UserRoleAPIPayload userRolePayload = new UserRoleAPIPayload();
        userRolePayload = gson.fromJson(requestPayload, UserRoleAPIPayload.class);
        //Get all the landing pages of the user
        Users user = userRepository.getUserUsingUsername(userRolePayload.getPrincipal());
        String landingPage = userRepository.getUserLandingPageForAPI(user, userRolePayload.getAppType());

        //Check if the response is null
        if (landingPage == null) {
            return "No User Group";
        }
        return landingPage;
    }

    @Override
    public String getUserRolesForAPI(String requestPayload) {
        UserRoleAPIPayload userRolePayload = new UserRoleAPIPayload();
        userRolePayload = gson.fromJson(requestPayload, UserRoleAPIPayload.class);
        Users user = userRepository.getUserUsingUsername(userRolePayload.getPrincipal());

        //Get the user role group
        String userRoleGroup = userRepository.getRoleGroupOfUser(user.getUsername(), userRolePayload.getAppType());

        if (userRoleGroup == null || userRoleGroup.equals("No Role")) {
            return "No Role";
        }
        //Retrieve the roles beloging to the group
        List<String> roles = userRepository.getRolesForGroup(userRoleGroup);
        return String.join(",", roles);
    }

    @Override
    public String getBranchCodeForAPI(String requestPayload) {
        SingleFieldPayload fieldPayload = new SingleFieldPayload();
        fieldPayload = gson.fromJson(requestPayload, SingleFieldPayload.class);

        String branchCode = userRepository.getBranchCode(fieldPayload.getFieldName());

        if (branchCode == null) {
            return "No Branch";
        }
        return branchCode;
    }

    @Override
    public String getPrincipalBranchesForAPI(String requestPayload) {
        UserRoleAPIPayload userRolePayload = new UserRoleAPIPayload();
        userRolePayload = gson.fromJson(requestPayload, UserRoleAPIPayload.class);
        Users user = userRepository.getUserUsingUsername(userRolePayload.getPrincipal());
        return user.getBranch().trim();
    }

    @Override
    public String getPrincipalLimitsForAPI(String requestPayload) {
        UserRoleAPIPayload userRolePayload = new UserRoleAPIPayload();
        userRolePayload = gson.fromJson(requestPayload, UserRoleAPIPayload.class);
        Users user = userRepository.getUserUsingUsername(userRolePayload.getPrincipal());
        return Integer.toString(user.getApprovalLevel());
    }

    @Override
    public String getPasswordChangeForAPI(String requestPayload) {
        LoginPayload loginPayload = new LoginPayload();
        loginPayload = gson.fromJson(requestPayload, LoginPayload.class);
        String newPassword = bCryptEncoder.encode(loginPayload.getPassword());
        Users selectedUser = userRepository.getUserUsingUsername(loginPayload.getUsername());
        selectedUser.setPassword(newPassword);
        int passwordChangeDate = Integer.parseInt(userRepository.getSystemParameterUsingName("Password_Change_Date").getParamValue());
        selectedUser.setPasswordChangeDate(selectedUser.getPasswordChangeDate().plusDays(passwordChangeDate));
        Users passwordUpdate = userRepository.updateUser(selectedUser);
        if (passwordUpdate != null) {
            return messageSource.getMessage("appMessages.passwordChangeSuccessful", new Object[0], Locale.ENGLISH);
        }
        return messageSource.getMessage("appMessages.passwordChangeFailed", new Object[0], Locale.ENGLISH);
    }

    @Override
    public String getSystemParameterForAPI(String requestPayload) {
        SingleFieldPayload fieldPayload = new SingleFieldPayload();
        fieldPayload = gson.fromJson(requestPayload, SingleFieldPayload.class);

        String paramName = userRepository.getParameter(fieldPayload.getFieldName());

        if (paramName == null) {
            return "No Param";
        }
        return paramName;
    }

    @Override
    public String getApprovalLevelForAPI(String requestPayload) {
        SingleFieldPayload fieldPayload = new SingleFieldPayload();
        fieldPayload = gson.fromJson(requestPayload, SingleFieldPayload.class);

        //The fieldName is the amount passed
        Collection<ApprovalLevel> approvalLevel = userRepository.getApprovalLevel();

        if (approvalLevel == null) {
            return "0";
        }

        for (ApprovalLevel app : approvalLevel) {
            BigDecimal lowerLimit = new BigDecimal(app.getLowerLimit().trim());
            BigDecimal upperLimit = new BigDecimal(app.getUpperLimit().trim());
            BigDecimal amount = fieldPayload.getAmount();
            if (amount.compareTo(lowerLimit) >= 0 && amount.compareTo(upperLimit) <= 0) {
                String appLevel = Integer.toString(app.getLevelCode());
                return appLevel.trim();
            }
        }
        return "0";
    }

    @Override
    public String changePassword(LoginPayload loginPayload, String principal) {
        //Check password complexity
        Boolean passwordMatch = validatePassword(loginPayload.getPassword());
        if (!passwordMatch) {
            return messageSource.getMessage("appMessages.passwordComplexityFail", new Object[0], Locale.ENGLISH);
        }

        //Check if the password matches
        if (!loginPayload.getPassword().equals(loginPayload.getConfirmPassword())) {
            return messageSource.getMessage("appMessages.passwordMismatch", new Object[0], Locale.ENGLISH);
        }
        String newPassword = bCryptEncoder.encode(loginPayload.getPassword());
        Users selectedUser = userRepository.getUserUsingUsername(principal);
        selectedUser.setPassword(newPassword);
        int passwordChangeDate = Integer.parseInt(userRepository.getSystemParameterUsingName("Password_Change_Date").getParamValue());
        selectedUser.setPasswordChangeDate(selectedUser.getPasswordChangeDate().plusDays(passwordChangeDate));
        Users passwordUpdate = userRepository.updateUser(selectedUser);
        if (passwordUpdate != null) {
            //Create an audit log
            AuditLog auditLog = new AuditLog();
            auditLog.setAuditClass("Profile");
            auditLog.setActionDescription("Password for " + selectedUser + " changed successfully");
            auditLog.setAuditType("Update");
            auditLog.setCreatedBy(principal);
            auditLog.setSystemDate(LocalDateTime.now());
            auditLog.setComputerName(computerHostName);
            auditLog.setDateCreated(LocalDateTime.now());
            auditLog.setAppType("Profile");
            createAuditLog(auditLog);
            return messageSource.getMessage("appMessages.passwordChangeSuccessful", new Object[0], Locale.ENGLISH);
        }
        return messageSource.getMessage("appMessages.passwordChangeFailed", new Object[0], Locale.ENGLISH);
    }

    @Override
    public int[] getUserStatistics() {
        int[] users = new int[6];
        List<Users> userList = userRepository.getUserProfiles();
        users[0] = userList.stream().collect(Collectors.toList()).size();
        users[1] = userList.stream().filter(u -> u.getStatus().trim().equals("Enabled")).collect(Collectors.toList()).size();
        users[2] = userList.stream().filter(u -> u.getStatus().trim().equals("Disabled")).collect(Collectors.toList()).size();
////        users[3] = userList.stream().filter(u -> u.getUserType().trim().equals("Inputer")).collect(Collectors.toList()).size();
//        users[4] = userList.stream().filter(u -> u.getUserType().trim().equals("Authorizer")).collect(Collectors.toList()).size();
//        users[5] = userList.stream().filter(u -> u.getUserType().trim().equals("Control")).collect(Collectors.toList()).size();
        return users;
    }

    @Override
    public Collection<RoleGroup> getRoleGroupsByAppType(String appType) {
        //This returns all the groups create based on the app type
        return userRepository.getRoleGroupsByAppType(appType);
    }

    @Override
    public Collection<String> getRoleGroupsNameByAppType(String appType) {
        //This returns all the groups name create based on the app type
        return userRepository.getRoleGroupsNameByAppType(appType);
    }

    @Override
    public String updateRoleGroup(RolePayload rolePayload, String principal, String appType) {
        //Add group for which roles will attached
        if (rolePayload.getRoleGroupName().equals("Delete")) {
            //To deletes the group, check if the group is in use
            GroupRoles selectedGroup = userRepository.getGroupByGroupName(rolePayload.getRoleGroup(), appType);
            if (selectedGroup != null) {
                return messageSource.getMessage("appMessages.roleGroupInUse", new Object[0], Locale.ENGLISH);
            }

            RoleGroup selectedGroupByName = userRepository.getRoleGroupsByNameByAppType(rolePayload.getRoleGroup(), appType);
            RoleGroup deletedGroup = userRepository.dropRoleGroup(selectedGroupByName);
            if (deletedGroup != null) {
                //Create audit log
                AuditLog auditLog = new AuditLog();
                auditLog.setAuditClass("Role");
                auditLog.setActionDescription("Role Group " + rolePayload.getRoleGroup() + " deleted successfully ");
                auditLog.setAuditType("Delete");
                auditLog.setCreatedBy(principal);
                auditLog.setSystemDate(LocalDateTime.now());
                auditLog.setComputerName(computerHostName);
                auditLog.setDateCreated(LocalDateTime.now());
                auditLog.setAppType(appType);
                createAuditLog(auditLog);
                return messageSource.getMessage("appMessages.dropRecordSuccess", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.recordUpdateFailed", new Object[0], Locale.ENGLISH);
        }

        //Check if its update
        if (!rolePayload.getRoleGroupName().equals("") && !rolePayload.getRoleGroup().equals("")) {
            //Check if the group is in use by the user menus
            Collection<UserMenu> selectedGroup = userRepository.checkIfRoleGroupIsInUseByUser(rolePayload.getRoleGroupName());
            if (selectedGroup != null) {
                return messageSource.getMessage("appMessages.roleGroupInUse", new Object[0], Locale.ENGLISH);
            }

            //Check if the new role group is existing already
            GroupRoles proposedNewGroup = userRepository.getGroupByGroupName(rolePayload.getRoleGroup(), appType);
            if (proposedNewGroup != null) {
                return messageSource.getMessage("appMessages.roleGroupInUse", new Object[0], Locale.ENGLISH);
            }

            RoleGroup selectedGroupByName = userRepository.getRoleGroupsByNameByAppType(rolePayload.getRoleGroup(), appType);

            selectedGroupByName.setGroupName(rolePayload.getRoleGroupName());
            RoleGroup updateGroup = userRepository.updateRoleGroup(selectedGroupByName);
            if (updateGroup != null) {
                //Create and audit log
                AuditLog auditLog = new AuditLog();
                auditLog.setAuditClass("Role");
                auditLog.setActionDescription("Role Group name changed from " + selectedGroupByName.getGroupName() + " to " + rolePayload.getRoleGroupName());
                auditLog.setAuditType("Update");
                auditLog.setCreatedBy(principal);
                auditLog.setSystemDate(LocalDateTime.now());
                auditLog.setComputerName(computerHostName);
                auditLog.setDateCreated(LocalDateTime.now());
                auditLog.setAppType(appType);
                createAuditLog(auditLog);
                return messageSource.getMessage("appMessages.recordUpdatedSucceed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.recordUpdateFailed", new Object[0], Locale.ENGLISH);
        }

        //This checks if its a new group to be created
        if (!rolePayload.getRoleGroupName().equals("") && rolePayload.getRoleGroup().equals("")) {
            //Check if the role group exist for the same app type
            Collection<RoleGroup> roleGroup = userRepository.getRoleGroupsByAppType(rolePayload.getRoleGroupName(), appType);
            if (roleGroup != null) {
                return messageSource.getMessage("appMessages.roleGroupExist", new Object[0], Locale.ENGLISH);
            }

            RoleGroup newGroupRole = new RoleGroup();
            newGroupRole.setAppType(appType);
            newGroupRole.setCreatedBy(principal);
            newGroupRole.setDateCreated(LocalDateTime.now());
            newGroupRole.setGroupName(rolePayload.getRoleGroupName());
            RoleGroup newGroup = userRepository.createRoleGroup(newGroupRole);

            if (newGroup != null) {
                //Create an audit log
                AuditLog auditLog = new AuditLog();
                auditLog.setAuditClass("Role");
                auditLog.setActionDescription("Role Group " + rolePayload.getRoleGroupName() + " created successfully");
                auditLog.setAuditType("Create");
                auditLog.setCreatedBy(principal);
                auditLog.setSystemDate(LocalDateTime.now());
                auditLog.setComputerName(computerHostName);
                auditLog.setDateCreated(LocalDateTime.now());
                auditLog.setAppType(appType);
                createAuditLog(auditLog);
                return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
        }
        return null;
    }

    @Override
    public String dropRoleGroup(SingleFieldPayload roleDropPayload, String principal) {
        //Check if roles are selected
        if (roleDropPayload.getFieldName() == null) {
            return messageSource.getMessage("appMessages.noRoleSelected", new Object[0], Locale.ENGLISH);
        }
        String[] selectedRoles = roleDropPayload.getFieldName().split(",");

        for (String role : selectedRoles) {
            GroupRoles roleToDelete = userRepository.getGroupRolesUsignId(new Long(role));
            if (roleToDelete != null) {
                GroupRoles selectedRole = userRepository.dropGroupRole(roleToDelete);
            }
        }

        //Create an audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditClass("Role");
        auditLog.setActionDescription("The selected role(s) " + StringUtils.join(selectedRoles, ",") + " deleted from the Role ");
        auditLog.setAuditType("Delete");
        auditLog.setCreatedBy(principal);
        auditLog.setSystemDate(LocalDateTime.now());
        auditLog.setComputerName(computerHostName);
        auditLog.setDateCreated(LocalDateTime.now());
        auditLog.setAppType("Role");
        createAuditLog(auditLog);
        return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
    }

    @Override
    public Collection<GroupRoles> getRolesGroupedByAppType(String appType) {
        //Return all the roles and their groups based on the application type
        return userRepository.getRolesGroupedByAppType(appType);
    }

    @Override
    public String addRoleToGroup(RolePayload rolePayload, String principal, String appType) {
        //Get all the selected roles
        String[] selectedRoles = rolePayload.getRoles().split(",");

        if (selectedRoles.length <= 0) {
            return messageSource.getMessage("appMessages.noRoleSelected", new Object[0], Locale.ENGLISH);
        }

        for (String role : selectedRoles) {
            //Check if the group has selected role already
            GroupRoles selectedRole = userRepository.checkIfRoleExistInGroup(rolePayload.getRoleGroupName(), role, appType);
            if (selectedRole == null) {
                GroupRoles newGroupRole = new GroupRoles();
                newGroupRole.setAppType(appType);
                newGroupRole.setCreatedBy(principal);
                newGroupRole.setDateCreated(LocalDateTime.now());
                newGroupRole.setRoleGroup(rolePayload.getRoleGroupName());
                newGroupRole.setRoleName(role);
                GroupRoles groupRole = userRepository.createGroupRole(newGroupRole);
            }
        }

        //Create an audit log
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditClass("Role");
        auditLog.setActionDescription("The selected role(s) " + StringUtils.join(selectedRoles, ",") + " mapped to the Role Group " + rolePayload.getRoleGroupName());
        auditLog.setAuditType("Create");
        auditLog.setCreatedBy(principal);
        auditLog.setSystemDate(LocalDateTime.now());
        auditLog.setComputerName(computerHostName);
        auditLog.setDateCreated(LocalDateTime.now());
        auditLog.setAppType(appType);
        createAuditLog(auditLog);
        return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);

    }

    @Override
    public Collection<String> getRoleGroups(String appType) {
        //Returns all the group roles based on the app type
        return userRepository.getRoleGroupsNameByAppType(appType);
    }

    private void updateAuditLog(String auditClass, String actionDescription, String auditType, String createdBy, String appType, String approvedBy) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAuditClass(auditClass);
        auditLog.setActionDescription(actionDescription);
        auditLog.setAuditType(auditType);
        auditLog.setCreatedBy(createdBy);
        auditLog.setSystemDate(LocalDateTime.now());
        auditLog.setComputerName(computerHostName);
        auditLog.setDateCreated(LocalDateTime.now());
        auditLog.setAppType(appType);
        auditLog.setApprovedBy(approvedBy);
        auditLog.setDateApproved(LocalDateTime.now());
        createAuditLog(auditLog);
    }

    @Override
    public String approveUserProfile(Long id, String principal) {
        //Get the details of the transaction to be approved
        UserTemp trans = userRepository.getUserTempUsingId(id);
        if (trans == null) {
            return messageSource.getMessage("appMessages.recordNotFound", new Object[0], Locale.ENGLISH);
        }

        //Get the user involved
        Users selectedUser = userRepository.getUserUsingUsername(trans.getUserId());
        if (selectedUser == null) {
            return messageSource.getMessage("appMessages.invalidUsername", new Object[0], Locale.ENGLISH);
        }

        //Create a role group for the user
        if (trans.getTransType().equals("New User Role")) {
            UserMenu newUserMenu = new UserMenu();
            newUserMenu.setAppType(trans.getAppType());
            newUserMenu.setCreatedBy(principal);
            newUserMenu.setDateCreated(LocalDateTime.now());
            newUserMenu.setUsername(trans.getUserId());
            newUserMenu.setMenuRole(trans.getNewValue());
            UserMenu userMenu = userRepository.createUserMenu(newUserMenu);
            //Create an audit log
            updateAuditLog("Role", trans.getUserId() + " added to the Role Group " + trans.getNewValue(), "Create", trans.getCreatedBy(), trans.getAppType(), principal);

            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Update a role group for the user
        if (trans.getTransType().equals("Update User Role")) {
            UserMenu selectedMenu = userRepository.getUserRoleGroupUsingUsername(trans.getUserId(), trans.getAppType());
            String oldRole = selectedMenu.getMenuRole();
            selectedMenu.setMenuRole(trans.getNewValue());
            UserMenu updatedMenu = userRepository.updateUserRoleGroup(selectedMenu);
            //Create an audit log

            updateAuditLog("Role", trans.getUserId() + " Role Group changed from " + oldRole + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), trans.getAppType(), principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Create a new landing page
        if (trans.getTransType().equals("New Landing Page")) {
            LandingPage landingPage = userRepository.getLandingPageUsingRoleName(trans.getNewValue(), trans.getAppType());

            UserRole selectedUserRole = userRepository.checkIfPrincipalHasRole(selectedUser, trans.getAppType());
            UserRole newUserRole = new UserRole();
            newUserRole.setLandingPageId(landingPage);
            newUserRole.setUsersId(selectedUser);
            newUserRole.setDateCreated(LocalDateTime.now());
            newUserRole.setCreatedBy(principal);
            newUserRole.setDateCreated(LocalDateTime.now());
            newUserRole.setStatus("Active");
            selectedUserRole = userRepository.createUserRole(newUserRole);
            if (selectedUserRole == null) {
                return messageSource.getMessage("appMessages.recordAddFailed", new Object[0], Locale.ENGLISH);
            }

            //Create an audit log
            updateAuditLog("Role", "Landing Page " + landingPage.getRoleName() + " created for user " + selectedUser.getUsername(), "Create", trans.getCreatedBy(), trans.getAppType(), principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);

        }

        //Change the user landing page
        if (trans.getTransType().equals("Update Landing Page")) {
            LandingPage landingPage = userRepository.getLandingPageUsingRoleName(trans.getNewValue(), trans.getAppType());
            UserRole selectedUserRole = userRepository.checkIfPrincipalHasRole(selectedUser, trans.getAppType());
            String oldLandingPage = selectedUserRole.getLandingPageId().getRoleName();
            selectedUserRole.setLandingPageId(landingPage);
            selectedUserRole.setUsersId(selectedUser);
            selectedUserRole.setDateCreated(LocalDateTime.now());
            selectedUserRole.setCreatedBy(principal);
            selectedUserRole.setDateCreated(LocalDateTime.now());
            selectedUserRole = userRepository.updateUserRole(selectedUserRole);
            if (selectedUserRole == null) {
                return messageSource.getMessage("appMessages.recordUpdateFailed", new Object[0], Locale.ENGLISH);
            }

            //Create an audit log
            updateAuditLog("Role", "Landing Page for " + selectedUser.getUsername() + " changed from " + oldLandingPage + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), trans.getAppType(), principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Creating new profile
        if (trans.getTransType().equals("Create Profile")) {
            selectedUser.setStatus("Enabled");
            selectedUser.setPasswordChangeDate(LocalDate.now().minusDays(1));
            Users updatedUser = userRepository.updateUser(selectedUser);

            //Create audit log
            updateAuditLog("Profile", "Profile created for " + selectedUser.getLastName() + ", " + selectedUser.getOtherName() + " (" + selectedUser.getUsername() + ") " + " in branch " + selectedUser.getBranch(), "Create", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            try {
                //Send an email with the details
                sendEmail(selectedUser.getUsername(), selectedUser.getDefaultPassword(), selectedUser.getBranch(), selectedUser.getEmail(), selectedUser.getLastName() + ", " + selectedUser.getOtherName(), LocalDateTime.now().toString());
            } catch (MessagingException ex) {
                java.util.logging.Logger.getLogger(UserServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of approval limit
        if (trans.getTransType().equals("Approval Level")) {
            String oldValue = Integer.toString(selectedUser.getApprovalLevel());
            //Get the approval level
            selectedUser.setApprovalLevel(Integer.parseInt(trans.getNewValue()));
            Users updatedUser = userRepository.updateUser(selectedUser);

            //Create audit log
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". Approval Level changed from " + oldValue + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of status to enable
        if (trans.getTransType().equals("Enable Profile")) {
            selectedUser.setStatus("Enabled");
            Users updatedUser = userRepository.updateUser(selectedUser);

            //Create audit log
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". Profile enabled", "Update", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of status to disable
        if (trans.getTransType().equals("Disable Profile")) {
            selectedUser.setStatus("Disabled");
            Users updatedUser = userRepository.updateUser(selectedUser);

            //Create audit log
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". Profile disabled", "Update", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of or assignment of branch
        if (trans.getTransType().equals("Branch")) {
            String oldValue = selectedUser.getBranch();
            selectedUser.setBranch(trans.getNewValue());
            Users updatedUser = userRepository.updateUser(selectedUser);
            //Create audit log
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". User branch changed from " + oldValue + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of or assignment of user type
        if (trans.getTransType().equals("User Type")) {
            String oldValue = selectedUser.getUserType();
            selectedUser.setUserType(trans.getNewValue());
            Users updatedUser = userRepository.updateUser(selectedUser);
            //Create audit log
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". User type changed from " + oldValue + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), "User Manager", principal);

            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of or assignment of last name
        if (trans.getTransType().equals("Last Name")) {
            String oldValue = selectedUser.getLastName();
            selectedUser.setLastName(trans.getNewValue());
            Users updatedUser = userRepository.updateUser(selectedUser);
            //Create audit log
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". Last name changed from " + oldValue + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of or assignment of other name
        if (trans.getTransType().equals("Other Name")) {
            String oldValue = selectedUser.getOtherName();
            selectedUser.setOtherName(trans.getNewValue());
            Users updatedUser = userRepository.updateUser(selectedUser);
            //Create audit log
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". Other name changed from " + oldValue + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of email address
        if (trans.getTransType().equals("Email")) {
            String oldValue = selectedUser.getEmail();
            selectedUser.setEmail(trans.getNewValue());
            Users updatedUser = userRepository.updateUser(selectedUser);

            //Create audit log
            updateAuditLog("Profile", "Profile updated for " + selectedUser.getUsername() + ". Email changed from " + oldValue + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        //Change of email address
        if (trans.getTransType().equals("Password Reset")) {
            String oldValue = selectedUser.getDefaultPassword();
            selectedUser.setDefaultPassword(trans.getNewValue());
            selectedUser.setPassword(bCryptEncoder.encode(trans.getNewValue()));
            selectedUser.setPasswordChangeDate(LocalDate.now().minusDays(1));
            selectedUser.setPasswordTry(0);
            Users updatedUser = userRepository.updateUser(selectedUser);

            //Create audit log
            updateAuditLog("Profile", "Profile password for " + selectedUser.getUsername() + ". Default Password changed from " + oldValue + " to " + trans.getNewValue(), "Update", trans.getCreatedBy(), "User Manager", principal);
            //Delete the record from user temp table
            UserTemp deletedTrans = userRepository.dropUserTemp(trans);
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }
        // Unknown error at this point
        return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
    }

    @Override
    public String dropUserProfile(Long id, String principal) {
        //Get the details of the transaction to be approved
        UserTemp trans = userRepository.getUserTempUsingId(id);
        if (trans == null) {
            return messageSource.getMessage("appMessages.recordNotFound", new Object[0], Locale.ENGLISH);
        }

        //Check if its profile and delete the profile first
        if (trans.getTransType().equals("Create Profile")) {
            Users user = userRepository.getUserProfileUsingId(new Long(trans.getInitialValue()));
            Users droppedUser = userRepository.dropUserProfile(user);
            UserTemp deletedRecord = userRepository.dropUserTemp(trans);
            if (droppedUser == null) {
                return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
            }
            return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
        }

        UserTemp deletedRecord = userRepository.dropUserTemp(trans);
        if (deletedRecord == null) {
            return messageSource.getMessage("appMessages.operationFailed", new Object[0], Locale.ENGLISH);
        }
        //Create audit log
        updateAuditLog("Profile", trans.getTransType() + " record for " + trans.getUserId() + " deleted", "Delete", trans.getCreatedBy(), "User Manager", principal);
        return messageSource.getMessage("appMessages.operationSuccessful", new Object[0], Locale.ENGLISH);
    }

    @Override
    public Collection<UserTemp> getPendingApproval() {
        return userRepository.getPendingProfileApproval();
    }

    private boolean validatePassword(final String password) {
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    @Override
    public String changeDefaultPassword(LoginPayload loginPayload) {
        //Check the current password
        UserDetails auth = userDetailService.loadUserByUsername(loginPayload.getUsername());
        Boolean currentPasswordValid = bCryptEncoder.matches(loginPayload.getPassword(), auth.getPassword());
        if (!currentPasswordValid) {
            return messageSource.getMessage("appMessages.invalidCurrentPassword", new Object[0], Locale.ENGLISH);
        }

        //Check password complexity
        Boolean passwordMatch = validatePassword(loginPayload.getNewPassword());
        if (!passwordMatch) {
            return messageSource.getMessage("appMessages.passwordComplexityFail", new Object[0], Locale.ENGLISH);
        }

        //Check if the password matches
        if (!loginPayload.getNewPassword().equals(loginPayload.getConfirmPassword())) {
            return messageSource.getMessage("appMessages.passwordMismatch", new Object[0], Locale.ENGLISH);
        }

        String newPassword = bCryptEncoder.encode(loginPayload.getNewPassword());
        Users selectedUser = userRepository.getUserUsingUsername(loginPayload.getUsername());
        selectedUser.setPassword(newPassword);
        selectedUser.setPasswordChangeDate(LocalDate.now().plusDays(Integer.parseInt(getSystemParameter("Password_Change_Date"))));
        Users passwordUpdate = userRepository.updateUser(selectedUser);
        if (passwordUpdate != null) {
            //Create an audit log
            AuditLog auditLog = new AuditLog();
            auditLog.setAuditClass("Profile");
            auditLog.setActionDescription("Password for " + selectedUser + " changed successfully");
            auditLog.setAuditType("Update");
            auditLog.setCreatedBy(loginPayload.getUsername());
            auditLog.setSystemDate(LocalDateTime.now());
            auditLog.setComputerName(computerHostName);
            auditLog.setDateCreated(LocalDateTime.now());
            auditLog.setAppType("Profile");
            createAuditLog(auditLog);
            return messageSource.getMessage("appMessages.passwordChangeSuccessful", new Object[0], Locale.ENGLISH);
        }
        return messageSource.getMessage("appMessages.passwordChangeFailed", new Object[0], Locale.ENGLISH);
    }

    public void sendEmail(String username, String password, String branch, String recipientAddress, String clientName, String date) throws AddressException, MessagingException {
        try {
            //Use this to send verification email confirmation
            Authenticator auth = new Authenticator() {
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(env.getProperty("accion.email.sender"), env.getProperty("accion.email.password"));
                }
            };
            Properties properties = System.getProperties();
            properties.put("mail.smtp.host", "smtp.office365.com");
            properties.put("mail.smtp.port", 587);
            properties.put("protocol", "smtps");
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            Session session = Session.getInstance(properties, auth);

            String message = "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "    <head>\n"
                    + "        <title>Accion IS</title>\n"
                    + "        <meta charset=\"UTF-8\">\n"
                    + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                    + "    </head>\n"
                    + "    <body>\n"
                    + "        <div style=\"background-color: white; position:absolute; left:20%; top:1%;width:60%;border:2px solid #ea7600; border-radius:4px; padding:5px;\">\n"
                    + "                <h3 style=\"color:#ea7600;\">Profile Notification</h3><hr/>\n"
                    + "                <h4 style=\"color:#ea7600;\">A profile has been created for <span style=\"color:ea7600;\">" + clientName + " </span> with the following details\n</h4>\n"
                    + "             <table>\n"
                    + "                <thead>\n"
                    + "\n"
                    + "                </thead>\n"
                    + "                <tbody>\n"
                    + "                    <tr>\n"
                    + "                        <td style=\"color:gray;\">Login Username : </td>\n"
                    + "                        <td style=\"color:#ea7600;\">&nbsp;" + username + "</td>\n"
                    + "                    </tr>\n"
                    + "                    <tr>\n"
                    + "                        <td style=\"color:gray;\">Default Password : </td>\n"
                    + "                        <td style=\"color:#ea7600;\">&nbsp;" + password + "</td>\n"
                    + "                    </tr>\n"
                    + "                    <tr>\n"
                    + "                        <td style=\"color:gray;\">Assigned Branch : </td>\n"
                    + "                        <td style=\"color:#ea7600;\">&nbsp;" + branch + "</td>\n"
                    + "                    </tr>\n"
                    + "                </tbody>\n"
                    + "            </table>\n"
                    + "		<p style=\"color:gray;\">Kindly visit the Accion Integration Service home page (http://10.10.0.20:8080/accion), select the desired platform and change the auto generated password when prompt to do so.</p>\n"
                    + "		<p style=\"color:gray;\">You may also visit the Accion Integration Service home page by clicking on the shortcut on your desktop.</p>\n"
                    + "		<p style=\"color:gray;\">Thank you</p>\n"
                    + "		<p style=\"color:gray;\">Digital Systems Development Unit</p>\n"
                    + "		<p style=\"color:#ea7600;\">For enquiry relating to your user profile on this platform, kindly call 234 1 2719325-6 (Ext 137)</p>\n"
                    + "    </body>\n"
                    + "</html>";

            // creates a new e-mail message
            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(env.getProperty("accion.email.sender")));
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientAddress));
            msg.setSubject(env.getProperty("accion.email.subject"));
            msg.setSentDate(new Date());
            // set html text message
            msg.setContent(message, "text/html");

            // sends the e-mail
            Transport.send(msg);

        } catch (MessagingException exception) {
            System.out.print(exception);
        }
    }

    @Override
    public void updateUserAsOnline(String payload) {
        LoginPayload loginPayload = gson.fromJson(payload, LoginPayload.class);
        if (loginPayload.getUserOnlineStatus()) {
            Users selectedUser = userRepository.getUserUsingUsername(loginPayload.getUsername());
            if (selectedUser != null) {
                selectedUser.setUserOnline(loginPayload.getUserOnlineStatus());
                selectedUser.setSessionId(loginPayload.getSessionId());
                Users user = userRepository.updateUser(selectedUser);
            }
        }
    }

    //@Scheduled(fixedRate = 60000)
    public void resetLockedUsers() {
        List<Users> lockedUsers = userRepository.getDisabledUsers().stream().filter(t -> LocalDateTime.now().isAfter(t.getResetTime())).collect(Collectors.toList());
        for (Users user : lockedUsers) {
            user.setStatus("Enabled");
            Users updatedUser = userRepository.updateUser(user);
        }
        System.out.print("Locked Users Reset Successfully");
    }

    @Override
    public Collection<ApprovalLevel> getApprovalLevels() {
        return userRepository.getApprovalLevel();
    }

    @Override
    public String updateApprovalLevel(UserProfilePayload userProfilePayload, String principal) {
        ApprovalLevel approval = userRepository.getApprovalLevelUsingLevelCode(userProfilePayload.getApprovalLevel());
        //Check if the upper and lower limits are the same
        if (userProfilePayload.getLowerLimit().equals(userProfilePayload.getUpperLimit())) {
            return messageSource.getMessage("appMessages.sameApprovalLimit", new Object[0], Locale.ENGLISH);
        }

        if (new Long(userProfilePayload.getLowerLimit()) > new Long(userProfilePayload.getUpperLimit())) {
            return messageSource.getMessage("appMessages.invertedApprovalLimit", new Object[0], Locale.ENGLISH);
        }

        if (approval == null) {
            //This means there is no approval level already. Create new on
            ApprovalLevel approvalLevel = new ApprovalLevel();
            approvalLevel.setLevelCode(Integer.getInteger(userProfilePayload.getApprovalLevel()));
            approvalLevel.setLowerLimit(userProfilePayload.getLowerLimit().replace(",", ""));
            approvalLevel.setUpperLimit(userProfilePayload.getUpperLimit().replace(",", ""));
            ApprovalLevel createdApprovalLevel = userRepository.createApprovalLevel(approvalLevel);
            return messageSource.getMessage("", new Object[0], Locale.ENGLISH);
        }
        approval.setLevelCode(new Integer(userProfilePayload.getApprovalLevel()));
        approval.setLowerLimit(userProfilePayload.getLowerLimit().replace(",", ""));
        approval.setUpperLimit(userProfilePayload.getUpperLimit().replace(",", ""));
        ApprovalLevel updatedApprovalLevel = userRepository.updateApprovalLevel(approval);
        return messageSource.getMessage("appMessages.recordUpdatedSucceed", new Object[0], Locale.ENGLISH);
    }

    @Override
    public Collection<Promotion> getPromotionList() {
        return userRepository.getPromotionList();
    }

    private boolean validateControlPassword(final String password) {
        controlMatcher = controlPattern.matcher(password);
        return controlMatcher.matches();
    }

    @Override
    public String encryptPassword(LoginPayload loginPayload, String principal) {
        //Check the first password complexity is okay
//        Boolean firstPasswordMatch = validateControlPassword(loginPayload.getPassword());
//        if (!firstPasswordMatch) {
//            return messageSource.getMessage("appMessages.passwordComplexityFail", new Object[0], Locale.ENGLISH);
//        }
//
//        //Check the first password complexity is okay
//        Boolean secondPasswordMatch = validateControlPassword(loginPayload.getPassword2());
//        if (!secondPasswordMatch) {
//            return messageSource.getMessage("appMessages.passwordComplexityFail", new Object[0], Locale.ENGLISH);
//        }

        //Check if the first password matches
        if (!loginPayload.getPassword().equals(loginPayload.getConfirmPassword())) {
            return messageSource.getMessage("appMessages.passwordMismatch", new Object[0], Locale.ENGLISH);
        }

        //Check if the second password matches
        if (!loginPayload.getPassword2().equals(loginPayload.getConfirmPassword2())) {
            return messageSource.getMessage("appMessages.passwordMismatch", new Object[0], Locale.ENGLISH);
        }

        String newJoinedPassword = loginPayload.getPassword().concat(loginPayload.getPassword2());
        String newPassword = bCryptEncoder.encode(newJoinedPassword);

        //Set the path to the text file
        String path = servletContext.getRealPath("/") + "upload" + File.separator + "sample.txt";
        try {
            FileWriter write = new FileWriter(path, true);
            PrintWriter printer = new PrintWriter(write);
            printer.println(loginPayload.getUsername() + "-" + newJoinedPassword);
            printer.close();
        } catch (Exception ex) {
            return messageSource.getMessage("appMessages.passwordEncryptionFailed", new Object[0], Locale.ENGLISH);
        }
        return messageSource.getMessage("appMessages.passwordEncryptionSucceed", new Object[0], Locale.ENGLISH) + " " + newPassword;
    }

    @Override
    public String getUserDetailsForAPI(String requestPayload) {
        LoginPayload loginPayload = new LoginPayload();
        loginPayload = gson.fromJson(requestPayload, LoginPayload.class);
        Users user = userRepository.getUserUsingUsername(loginPayload.getUsername());

        if (user == null) {
            return "";
        }
        Users newUser = new Users();
        newUser.setEmail(user.getEmail());
        newUser.setLastLogin(user.getLastLogin());
        newUser.setLastName(user.getLastName());
        newUser.setOtherName(user.getOtherName());
        newUser.setPasswordChangeDate(user.getPasswordChangeDate());
        newUser.setUserType(user.getUserType());
        newUser.setResetTime(user.getResetTime());
        newUser.setUserOnline(user.getUserOnline());
        return gson.toJson(newUser, Users.class);
    }

    @Override
    public String getAuthorizersEmailForAPI(String requestPayload) {
        SingleFieldPayload fieldPayload = new SingleFieldPayload();
        fieldPayload = gson.fromJson(requestPayload, SingleFieldPayload.class);

        //The fieldName is the amount passed
        String branchName = fieldPayload.getFieldName();

        List<Users> users = userRepository.getAuthorizersUsingBranch(branchName);
        if (users != null) {
            List<String> authEmails = new ArrayList<>();
            for (Users user : users) {
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    authEmails.add(user.getEmail().trim());
                }
            }
            return String.join(",", authEmails);
        }
        return "bokon@accionmfb.com";
    }

    @Override
    public String getUserListForAPI() {
        List<String> userList = userRepository.getActiveUsers();
        return String.join(",", userList);
    }

    void updatePasswordTry(String username) {
        //Update user password try
        Users user = userRepository.getUserUsingUsername(username);
        user.setPasswordTry(user.getPasswordTry() + 1);
        Users updatedUser = userRepository.updateUser(user);
    }

    @Override
    public String getUserPasswordTry(String username) {
        Users user = userRepository.getUserUsingUsername(username);
        if (user == null) {
            return null;
        }

        return String.valueOf(user.getPasswordTry());
    }
}
