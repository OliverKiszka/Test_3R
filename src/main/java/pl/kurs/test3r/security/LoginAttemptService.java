package pl.kurs.test3r.security;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration WINDOW = Duration.ofMinutes(5);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final Clock clock;
    private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> locks = new ConcurrentHashMap<>();

    public LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public void recordSuccessfulLogin(String username) {
        if (username == null) {
            return;
        }
        attempts.remove(username);
        locks.remove(username);
    }

    public Optional<Instant> recordFailedLogin(String username) {
        if (username == null) {
            return Optional.empty();
        }
        if (isAccountLocked(username)) {
            return getLockExpiration(username);
        }
        Instant now = Instant.now(clock);
        Deque<Instant> timestamps = attempts.computeIfAbsent(username, key -> new ArrayDeque<>());
        synchronized (timestamps) {
            timestamps.addLast(now);
            pruneOldAttempts(timestamps, now.minus(WINDOW));
            if (timestamps.size() >= MAX_ATTEMPTS) {
                Instant unlockAt = now.plus(LOCK_DURATION);
                locks.put(username, unlockAt);
                return Optional.of(unlockAt);
            }
        }
        return Optional.empty();
    }

    public boolean isAccountLocked(String username) {
        if (username == null) {
            return false;
        }
        Instant unlockAt = locks.get(username);
        if (unlockAt == null) {
            return false;
        }
        Instant now = Instant.now(clock);
        if (now.isAfter(unlockAt)) {
            locks.remove(username);
            return false;
        }
        return true;

    }

    public Optional<Instant> getLockExpiration(String username) {
        if (username == null) {
            return Optional.empty();
        }
        Instant unlockAt = locks.get(username);
        if (unlockAt == null) {
            return Optional.empty();
        }
        if (Instant.now(clock).isAfter(unlockAt)) {
            locks.remove(username);
            attempts.remove(username);
            return Optional.empty();
        }
        return Optional.of(unlockAt);

    }

    private void pruneOldAttempts(Deque<Instant> timestamps, Instant threshhold) {
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(threshhold)) {
            timestamps.removeFirst();
        }
    }

}
