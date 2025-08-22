/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package com.accionmfb.accion;
//
//import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
//import java.io.IOException;
//import org.jasypt.encryption.StringEncryptor;
//import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
//import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
//import org.springframework.beans.BeansException;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Import;
//import org.springframework.context.support.ResourceBundleMessageSource;
//import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.web.multipart.commons.CommonsMultipartResolver;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
//import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
//import org.thymeleaf.spring5.SpringTemplateEngine;
//import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
//import org.thymeleaf.spring5.view.ThymeleafViewResolver;
//import org.thymeleaf.templatemode.TemplateMode;
//
///**
// *
// * @author Brian A. Okon - okon.brian@gmail.com
// */
//@Configuration
//@EnableWebMvc
//@EnableScheduling
//@EnableEncryptableProperties
//@ComponentScan(basePackages = "com.accionmfb.accion")
//@EnableJpaRepositories(basePackages = "com.accionmfb.accion.repository")
//@Import({WebSecurityConfig.class})
//
//public class SpringWebConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {
//
//    private ApplicationContext applicationContext;
//
//    @Bean(name = "multipartResolver")
//    public CommonsMultipartResolver getResolver() throws IOException {
//        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
//        //Set the maximum allowed size (in bytes) for each individual file.
//        resolver.setMaxUploadSizePerFile(5242880);//5MB
//        return resolver;
//    }
//
//    public SpringWebConfig() {
//        super();
//    }
//
//    @Override
//    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//
//    @Override
//    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
//        super.addResourceHandlers(registry);
//        registry.addResourceHandler("/images/**").addResourceLocations("/images/");
//        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
//        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
//    }
//
//    /*
//     *  Message externalization/internationalization
//     */
//    @Bean
//    public ResourceBundleMessageSource messageSource() {
//        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
//        messageSource.setBasename("messages");
//        messageSource.setUseCodeAsDefaultMessage(true);
//        messageSource.setDefaultEncoding("UTF-8");
//        messageSource.setCacheSeconds(0);
//        return messageSource;
//    }
//
//    @Bean
//    public SpringResourceTemplateResolver templateResolver() {
//        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
//        //templateResolver.setApplicationContext(this.applicationContext);
//        templateResolver.setPrefix("/WEB-INF/templates/");
//        templateResolver.setSuffix(".html");
//        templateResolver.setTemplateMode(TemplateMode.HTML);
//        templateResolver.setCacheable(true);
//        return templateResolver;
//    }
//
//    @Bean
//    public SpringTemplateEngine templateEngine() {
//        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
//        templateEngine.setTemplateResolver(templateResolver());
//        templateEngine.setEnableSpringELCompiler(true);
//        //TemplateEngine templateEngine = new TemplateEngine();
//        templateEngine.addDialect(new Java8TimeDialect());
//        return templateEngine;
//    }
//
//    @Bean
//    public ThymeleafViewResolver viewResolver() {
//        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
//        viewResolver.setTemplateEngine(templateEngine());
//        return viewResolver;
//    }
//
//    @Bean(name = "jasyptStringEncryptor")
//    public StringEncryptor getPasswordEncryptor() {
//        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
//        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
//        config.setPassword("H*-lLo5,e:2.VN"); // encryptor's private key
//        config.setAlgorithm("PBEWithMD5AndDES");
//        config.setKeyObtentionIterations("1000");
//        config.setPoolSize("1");
//        config.setProviderName("SunJCE");
//        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
//        config.setStringOutputType("base64");
//
//        encryptor.setConfig(config);
//        return encryptor;
//    }
//}


package com.accionmfb.accion;

// Remove this import - it's Spring Boot specific
// import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import java.io.IOException;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.spring31.properties.EncryptablePropertyPlaceholderConfigurer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 *
 * @author Brian A. Okon - okon.brian@gmail.com
 */
@Configuration
@EnableWebMvc
@EnableScheduling
// Remove this annotation - it's Spring Boot specific
// @EnableEncryptableProperties
@ComponentScan(basePackages = "com.accionmfb.accion")
@EnableJpaRepositories(basePackages = "com.accionmfb.accion.repository")
@Import({WebSecurityConfig.class})
public class SpringWebConfig extends WebMvcConfigurerAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver getResolver() throws IOException {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        //Set the maximum allowed size (in bytes) for each individual file.
        resolver.setMaxUploadSizePerFile(5242880);//5MB
        return resolver;
    }

    public SpringWebConfig() {
        super();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        registry.addResourceHandler("/images/**").addResourceLocations("/images/");
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
    }

    /*
     *  Message externalization/internationalization
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(0);
        return messageSource;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        //templateResolver.setApplicationContext(this.applicationContext);
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(true);
        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        //TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addDialect(new Java8TimeDialect());
        return templateEngine;
    }

    @Bean
    public ThymeleafViewResolver viewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        return viewResolver;
    }

//    @Bean(name = "jasyptStringEncryptor")
//    public  StringEncryptor getPasswordEncryptor() {
//        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
//        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
//        config.setPassword("H*-lLo5,e:2.VN"); // encryptor's private key
//        config.setAlgorithm("PBEWithMD5AndDES");
//        config.setKeyObtentionIterations("1000");
//        config.setPoolSize("1");
//        config.setProviderName("SunJCE");
//        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
//        config.setStringOutputType("base64");
//
//        encryptor.setConfig(config);
//        return encryptor;
//    }

    // Add this bean to handle encrypted properties in Spring Framework
    @Bean
    public static EncryptablePropertyPlaceholderConfigurer propertyConfigurer() {
        EncryptablePropertyPlaceholderConfigurer configurer =
                new EncryptablePropertyPlaceholderConfigurer(getPasswordEncryptor());

        // Add your property files here
        configurer.setLocations(
                new ClassPathResource("application.properties")
        );

        return configurer;
    }

    // Make this method static so it can be called from the static method above
    private static StringEncryptor getPasswordEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("H*-lLo5,e:2.VN"); // encryptor's private key
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");

        encryptor.setConfig(config);
        return encryptor;
    }
}