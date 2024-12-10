package com.example.onlineshop.configuration;

import com.example.onlineshop.service.AuthService;
import com.example.onlineshop.service.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class Security {
    UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/auth/signin", "/auth/signup", "/register").anonymous()
                                .requestMatchers("/shop").hasAnyAuthority("OWNER", "CUSTOMER")
                                .requestMatchers("/css/**", "/js/**", "/login").permitAll()
                                .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .accessDeniedHandler(
                        (request, response, accessDeniedException) -> {
                            if (request.getHeader("Accept").contains("text/html")) {
                                response.sendRedirect("/login");
                            } else {
                                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.getWriter().write("Access denied");
                            }
                        })
                        .authenticationEntryPoint((request, response, accessDeniedException) -> {
                            if (request.getHeader("Accept").contains("text/html")) {
                                response.sendRedirect("/login");
                            } else {
                                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.getWriter().write("Access denied");
                            }
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .failureUrl("/login?error=true")
                        .defaultSuccessUrl("/", true))
                .logout(logout ->
                        logout.logoutUrl("/logout").permitAll().logoutSuccessHandler((request, response, authentication) -> {
                            if (request.getHeader("Accept").contains("text/html")) {
                                response.sendRedirect("/login");
                            } else {
                                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                                if(authentication != null && authentication.isAuthenticated()){
                                    response.setStatus(HttpServletResponse.SC_OK);
                                    response.getWriter().write("Signed out successfully");
                                } else {
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    response.getWriter().write("You are not signed in");
                                }
                            }
                        }).invalidateHttpSession(true));

        return http.build();
    }
}
