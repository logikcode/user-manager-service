/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.accionmfb.accion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 *
 * @author Brian A. Okon - okon.brian@gmail.com
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/", "192.168.1.26:8080/572").permitAll()
                .antMatchers("/api/service/**").permitAll()
                .antMatchers("/login","/auth/login", "/user-manager/default-password/**", "/user-manager/change-default-password").permitAll()
                .antMatchers("/css/**", "/images/**", "/js/**", "/auth/login").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/").loginProcessingUrl("/")
                .defaultSuccessUrl("/home/client-dashboard", true)
                .failureForwardUrl("/login?error=true")
                .and()
                .logout().logoutSuccessUrl("/auth/logout")
                .deleteCookies("JSESSIONID")
                .and()
                .csrf().disable();
    }

    @Autowired
    public void init(AuthenticationManagerBuilder auth) throws Exception {

    }
}
