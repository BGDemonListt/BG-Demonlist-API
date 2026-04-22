package com.bgdl.bgdl.config;

import com.bgdl.bgdl.exceptions.user.UserNotFoundException;
import com.bgdl.bgdl.models.entity.BaseEntity;
import com.bgdl.bgdl.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for defining beans related to application setup, such as ModelMapper, ObjectMapper,
 * UserDetailsService, AuthenticationProvider, AuthenticationManager, PasswordEncoder, and RestTemplate.
 */
@Configuration
@RequiredArgsConstructor
@EnableAspectJAutoProxy
@EnableAsync
@EnableTransactionManagement
public class ApplicationConfig {
    private final UserRepository repository;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper
                .getConfiguration()
                .setPropertyCondition(context -> {
                    if (
                            !(context.getParent().getDestination() instanceof BaseEntity)
                    ) {
                        return true;
                    }

                    String destinationProperty = context.getMapping().getLastDestinationProperty().getName();

                    return !("id".equals(destinationProperty) ||
                            "createdAt".equals(destinationProperty) ||
                            "updatedAt".equals(destinationProperty) ||
                            "deletedAt".equals(destinationProperty));
                })
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE)
                .setMatchingStrategy(MatchingStrategies.STRICT);

        return modelMapper;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return objectMapper;
    }

    @Bean
    public Validator validator() {
        Validator validator;

        try (var factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        return validator;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repository.findByEmail(username)
                .orElseThrow(UserNotFoundException::new);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
