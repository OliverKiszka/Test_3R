package pl.kurs.test3r.security;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Optional;

public class LockingDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private final LoginAttemptService loginAttemptService;

    public LockingDaoAuthenticationProvider(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {

        String username = userDetails.getUsername();
        if (loginAttemptService.isAccountLocked(username)) {
            Optional<Instant> unlockAt = loginAttemptService.getLockExpiration(username);
            String message = unlockAt.map(instant -> "Account is locked untill " + instant)
                    .orElse("Account is locked");
            throw new LockedException(message);
        }


        super.additionalAuthenticationChecks(userDetails, authentication);
    }
}
