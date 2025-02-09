package com.cookiebytes.calmquest.config;


import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;


import static com.cookiebytes.calmquest.user.Role.*;
import static com.cookiebytes.calmquest.user.Permission.*;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers(

                        "/api/v1/auth/**",
                        "/v2/api-docs",
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-resources",
                        "/swagger-resources/**",
                        "/configuration/ui",
                        "/configuration/security",
                        "/swagger-ui/**",
                        "/webjars/**",
                        "/swagger-ui.html"
                )
                .permitAll()


                //What Admins Can Access
                .requestMatchers("/api/v1/admin/**").hasRole(ADMIN.name())

                 .requestMatchers(GET, "/api/v1/admin/**").
                        hasAuthority(ADMIN_READ.name())
                 .requestMatchers(POST, "/api/v1/admin/**").
                        hasAuthority(ADMIN_CREATE.name())
                 .requestMatchers(PUT, "/api/v1/admin/**").
                        hasAuthority(ADMIN_UPDATE.name())
                 .requestMatchers(DELETE, "/api/v1/admin/**").
                        hasAuthority(ADMIN_DELETE.name())

                //What Counselors Can Access
                .requestMatchers("/api/v1/counselor/**").hasAnyRole(ADMIN.name(),COUNSELOR.name())
                .requestMatchers(GET, "/api/v1/counselor/**").hasAnyAuthority(ADMIN_READ.name(), COUNSELOR_READ.name())
                .requestMatchers(POST, "/api/v1/counselor/**").hasAnyAuthority(ADMIN_CREATE.name(), COUNSELOR_CREATE.name())
                .requestMatchers(PUT, "/api/v1/counselor/**").hasAnyAuthority(ADMIN_UPDATE.name(), COUNSELOR_UPDATE.name())
                .requestMatchers(DELETE, "/api/v1/counselor/**").hasAnyAuthority(ADMIN_DELETE.name(), COUNSELOR_DELETE.name())

                //What Students Can Access
                .requestMatchers("/api/v1/student/**").hasAnyRole(ADMIN.name(), STUDENT.name())
                .requestMatchers(GET, "/api/v1/student/**").hasAnyAuthority(ADMIN_READ.name(), STUDENT_READ.name())
                .requestMatchers(POST, "/api/v1/student/**").hasAnyAuthority(ADMIN_CREATE.name(), STUDENT_CREATE.name())
                .requestMatchers(PUT, "/api/v1/student/**").hasAnyAuthority(ADMIN_UPDATE.name(), STUDENT_UPDATE.name())
                .requestMatchers(DELETE, "/api/v1/student/**").hasAnyAuthority(ADMIN_DELETE.name(), STUDENT_DELETE.name())




                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .logout()
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
        ;

        return http.build();
    }
}
