package com.zimono.sports_odds.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final AppProperties properties;

    public WebSecurityConfig(AppProperties properties) {
        this.properties = properties;
    }

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                // add filter before basic authentication triggers
                .addFilterBefore(new BypassSecurityFilter(properties), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().access((authentication, object) -> {
                            boolean enabled = properties.getSecurity().isEnabled();
//                            System.out.println("[DEBUG] enabled = " + enabled);
                            if (!enabled) {
                                return new AuthorizationDecision(true); // Permit All
                            }
                            boolean authenticated = authentication.get().isAuthenticated() &&
                                    !"anonymousUser".equals(authentication.get().getName());
                            return new AuthorizationDecision(authenticated);
                        })
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Does not cover the case when security is disabled and the request carries invalid
                // credentials, use custom failures entry point instead.
//                .httpBasic(Customizer.withDefaults());
                // Use custom failures entry point
                .httpBasic(basic ->
                        basic.authenticationEntryPoint((req, res, ex) -> {
                            if (properties.getSecurity().isEnabled()) {
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                            } else {
                                req.getRequestDispatcher(req.getServletPath()).forward(req, res);
                            }
                        })
                );

        return http.build();
    }

    /**
     * Based on configuration properties, this filter
     * cleans the security context so as basic auth as priority will drop
     * requests with invalid credentials even when security is off.
     */
    private static class BypassSecurityFilter extends OncePerRequestFilter {
        private final AppProperties properties;

        public BypassSecurityFilter(AppProperties properties) {
            this.properties = properties;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {

            if (!properties.getSecurity().isEnabled()) {
                // if security is off we need to clear the context
                // in the case the request provide faulty credentials
                SecurityContextHolder.clearContext();
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {

        UserDetails user1 = User.builder()
                .username("john")
                .password(encoder.encode("john"))
                .roles("USER")
                .build();
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user1, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
