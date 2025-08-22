/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion.controller;

import com.accionmfb.accion.payload.ResponsePayload;
import com.accionmfb.accion.service.UserService;
import com.google.gson.Gson;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Brian A. Okon okon.brian@gmail.com
 */
@RestController
public class AmisController {

    @Autowired
    UserService userService;

    private static final String BASE_URL = "/api/service";
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static String AUTHORIZATION = "";
    private static String SIGNATURE = "";
    private static String TIMESTAMP = "";

    ResponsePayload responsePayload;
    Gson gson;

    @Autowired
    Environment env;

    AmisController() {
        responsePayload = new ResponsePayload();
        gson = new Gson();
    }

    @PostMapping(value = BASE_URL + "/auth/login", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String login(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String loginPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
           // Sy
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Call the service to authenticate the user
        return userService.loginClient(loginPayload);
    }

    @PostMapping(value = BASE_URL + "/auth/login-status/update", consumes = "application/json")
    public void loginStatusUpdate(@RequestBody String loginPayload) {
        //Call the service to authenticate the user
        userService.updateUserAsOnline(loginPayload);
    }

    @PostMapping(value = BASE_URL + "/user/landing-page", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getUserLandingPage(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create the response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getUserLandingPageForAPI(requestPayload));
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @PostMapping(value = BASE_URL + "/user/menus", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getUserMenus(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getUserRolesForAPI(requestPayload));
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @PostMapping(value = BASE_URL + "/user/branchCode", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getPrincipalBranchCode(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getBranchCodeForAPI(requestPayload));
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @PostMapping(value = BASE_URL + "/user/branches", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getPrincipalBranches(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        return userService.getPrincipalBranchesForAPI(requestPayload);
    }

    @PostMapping(value = BASE_URL + "/user/limits", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getPrincipalLimit(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
        }

        //Create response payload
        return userService.getPrincipalLimitsForAPI(requestPayload);
    }

    @PostMapping(value = BASE_URL + "/user/change-password", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String changePassword(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
        }

        //Create response payload
        return userService.getPasswordChangeForAPI(requestPayload);
    }

    @PostMapping(value = BASE_URL + "/system/get-parameter", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getSystemParameter(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getSystemParameterForAPI(requestPayload));
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @PostMapping(value = BASE_URL + "/transaction/approval-level", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getApprovalLevel(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getApprovalLevelForAPI(requestPayload));
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @PostMapping(value = BASE_URL + "/transaction/authorizers", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getAuthorizersEmail(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getAuthorizersEmailForAPI(requestPayload));
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @PostMapping(value = BASE_URL + "/user-type", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getUserType(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getUserTypeForAPI(requestPayload));
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @PostMapping(value = BASE_URL + "/user/details", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public String getPrincipalFullName(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature, @RequestBody String requestPayload) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getUserDetailsForAPI(requestPayload));
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    @PostMapping(value = BASE_URL + "/user/list", produces = "application/json")
    @ResponseBody
    public String getUserList(@RequestHeader(value = "Authorization") String authorization, @RequestHeader("Signature") String signature) {
        Boolean requestHeaderValid = requestHeaderValid(authorization, signature);
        if (!requestHeaderValid) {
            //ResponsePayload authMessage = new ResponsePayload();
            responsePayload.setResponseCode("401");
            responsePayload.setResponseMessage("Authorization failed. Invalid header parameters");
            return null;
            //return gson.toJson(responsePayload, ResponsePayload.class);
        }

        //Create response payload
        responsePayload.setResponseCode("200");
        responsePayload.setResponseMessage(userService.getUserListForAPI());
        return gson.toJson(responsePayload, ResponsePayload.class);
    }

    private Boolean requestHeaderValid(String authorization, String signature) {
        //Check if the request header is valid
        Boolean headerValid = false;

        if (userService.getAuthorizationHeader().equals(authorization) && userService.getSignatureHeader().equals(signature)) {
            headerValid = true;
            return headerValid;
        }

        return headerValid;
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
}
