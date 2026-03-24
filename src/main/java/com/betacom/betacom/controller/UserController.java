package com.betacom.betacom.controller;

import com.betacom.betacom.dto.LoginRequestDto;
import com.betacom.betacom.dto.LoginResponse;
import com.betacom.betacom.dto.UserRegistrationDto;
import com.betacom.betacom.dto.UserResponseDto;
import com.betacom.betacom.service.UserService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${jwtToken.expirationTime}")
    private long expirationTime;

    @Value("${rate-limit.capacity}")
    private int capacity;

    @Value("${rate-limit.interval-seconds}")
    private int intervalSeconds;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequestDto, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, k -> createNewBucket());

        ConsumptionProbe consumptionProbe = bucket.tryConsumeAndReturnRemaining(1);

        if (consumptionProbe.isConsumed()) {
            String token = userService.login(loginRequestDto);
            return ResponseEntity.ok(new LoginResponse( token,  expirationTime / 1000));
        } else {
        long waitForRefillSeconds = consumptionProbe.getNanosToWaitForRefill() / 1_000_000_000;
        if (waitForRefillSeconds == 0) waitForRefillSeconds = 1;

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(waitForRefillSeconds))
                .body("Przekroczono limit prób logowania. Spróbuj ponownie za " + waitForRefillSeconds + " sekund.");
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        userService.registerUser(userRegistrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Konto zostało pomyślnie utworzone");
    }

    private Bucket createNewBucket() {
        Refill refill = Refill.intervally(capacity, Duration.ofSeconds(intervalSeconds));
        Bandwidth limit = Bandwidth.classic(capacity, refill);
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
