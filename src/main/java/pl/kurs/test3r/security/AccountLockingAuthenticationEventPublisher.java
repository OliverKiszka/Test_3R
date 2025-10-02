package pl.kurs.test3r.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.time.Instant;
import java.util.Optional;

public class AccountLockingAuthenticationEventPublisher implements AuthenticationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AccountLockingAuthenticationEventPublisher.class);

    private final LoginAttemptService loginAttemptService;

    public AccountLockingAuthenticationEventPublisher(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {
        if (authentication == null){
            return;
        }
        loginAttemptService.recordSuccessfulLogin(authentication.getName());
    }

    @Override
    public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
        if (authentication == null){
            return;
        }
        String username = extractUsername(authentication);
        Optional<Instant> unlockAt = loginAttemptService.recordFailedLogin(username);
        unlockAt.ifPresent(instant -> log.warn("User {} locked until {}", username, instant));
    }
    private String extractUsername(Authentication authentication){
        Object principal = authentication.getPrincipal();
        if (principal instanceof String username){
            return username;
        }
        return authentication.getName();
    }
}
