package com.Spring.Multi_Role_Job_Portal.Config;

import com.Spring.Multi_Role_Job_Portal.Security.JWTAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JWTAuthFilter jwtAuthFilter;

    //We make the security Filter Chain according to the JWT Auth Filter ----

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //First disabling csrf
        http.csrf(csrf -> csrf.disable())
                //Enabling stateless session management --
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //We are seeting some routes as public ----
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/register/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        //We get the get route of jobs as public too --- as anyone can see the jobs
                        .requestMatchers(HttpMethod.GET, "/jobs/**").permitAll()
                        //Remember we had used ROLE_...... in SimoleGrantedAuthority in User entity ---
                        //Spring will check from here --- the user ---
                        //All the routes of users are set to ADMIN only
                        .requestMatchers("/users").hasRole("ADMIN")
                        //All the routes of recruiter are reserved to the role RECRUITER only and thus for CANDIDATE
                        .requestMatchers("/recruiter/**").hasRole("RECRUITER")
                        .requestMatchers("/candidate/**").hasRole("CANDIDATE")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
