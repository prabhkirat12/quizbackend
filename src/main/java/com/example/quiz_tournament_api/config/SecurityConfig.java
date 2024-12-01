package com.example.quiz_tournament_api.config;

import com.example.quiz_tournament_api.services.CustomUserDetailsService;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "<>[\\]^`{|}");
            connector.setProperty("relaxedQueryChars", "<>[\\]^`{|}");
        });
    }

    @Bean
    public HttpFirewall defaultHttpFirewall() {
        return new DefaultHttpFirewall();
    }



    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(authenticationProvider())
                .build();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults()) // Enable CORS globally
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/users/auth/login").permitAll()
                        .requestMatchers("/quiz/all").permitAll()
                        .requestMatchers("/quiz/upcoming", "/quiz/past").permitAll()
                        .requestMatchers("/quiz/*/play").permitAll()
                        .requestMatchers("/quiz/categories").permitAll()
                        .requestMatchers(HttpMethod.POST, "/quiz/*/score").authenticated()
                        .requestMatchers(HttpMethod.POST, "/forgot-password").permitAll() // Add this line


                        // Restricted endpoints
                        .requestMatchers(HttpMethod.PUT, "/quiz/**").hasRole("ADMIN")    // Only ADMIN can update quizzes
                        .requestMatchers(HttpMethod.DELETE, "/quiz/**").hasRole("ADMIN")
                        .requestMatchers("/quiz/create").hasRole("ADMIN")
                        .requestMatchers("/quiz/active").hasAnyRole("ADMIN", "PLAYER")
                        .requestMatchers(HttpMethod.POST, "/quiz/*/score").hasAnyRole("ADMIN", "PLAYER")
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());

        http.setSharedObject(HttpFirewall.class, defaultHttpFirewall());

        return http.build();
    }
}
