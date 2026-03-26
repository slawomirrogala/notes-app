package com.betacom.betacom.service;

import com.betacom.betacom.dto.user.LoginRequest;
import com.betacom.betacom.dto.user.UserRegistration;
import com.betacom.betacom.dto.user.UserResponse;
import com.betacom.betacom.entity.User;
import com.betacom.betacom.exception.UserAlreadyExistsException;
import com.betacom.betacom.exception.UserNotFoundException;
import com.betacom.betacom.repository.UserRepository;
import com.betacom.betacom.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String login(LoginRequest loginRequest) {
        var user = userRepository.findByLogin(loginRequest.getLogin())
                .orElseThrow(() -> new BadCredentialsException(("Nieprawidłowy login lub hasło")));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(("Nieprawidłowy login lub hasło"));
        }

        return jwtService.generateToken(user.getLogin());
    }

    public UserResponse getUserById(UUID id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return new UserResponse(user.getId(), user.getLogin(), user.getCreatedAt());
    }

    public void registerUser(UserRegistration userRegistration) {
        if (userRepository.existsByLogin(userRegistration.getLogin())) {
            throw new UserAlreadyExistsException();
        }
        var user = User.builder()
                .login(userRegistration.getLogin())
                .password(passwordEncoder.encode(userRegistration.getPassword()))
                .build();

        userRepository.save(user);
    }
}
