package pl.kurs.test3r.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import pl.kurs.test3r.security.AccountLockingAuthenticationEventPublisher;
import pl.kurs.test3r.security.LockingDaoAuthenticationProvider;
import pl.kurs.test3r.security.LoginAttemptService;

import java.time.Clock;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername("admin")
                .password(passwordEncoder.encode("adminPass"))
                .roles("ADMIN", "EMPLOYEE", "IMPORTER")
                .build();

        UserDetails importer = User.withUsername("importer")
                .password(passwordEncoder.encode("importerPass"))
                .roles("IMPORTER")
                .build();

        UserDetails employee = User.withUsername("employee")
                .password(passwordEncoder.encode("employeePass"))
                .roles("EMPLOYEE")
                .build();

        return new InMemoryUserDetailsManager(admin, importer, employee);
    }

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(LoginAttemptService loginAttemptService) {
        return new AccountLockingAuthenticationEventPublisher(loginAttemptService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserDetailsService userDetailsService,
                                                   PasswordEncoder passwordEncoder,
                                                   LoginAttemptService loginAttemptService,
                                                   AuthenticationEventPublisher authenticationEventPublisher) throws Exception {

        var provider = new LockingDaoAuthenticationProvider(loginAttemptService);
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        http.setSharedObject(AuthenticationEventPublisher.class, authenticationEventPublisher);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(provider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/persons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/persons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/persons/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers("/api/imports/persons/**").hasAnyRole("ADMIN", "IMPORTER")
                        .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

}
