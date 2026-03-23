package com.betacom.betacom.service;

import com.betacom.betacom.dto.UserRegistrationDto;
import com.betacom.betacom.dto.UserResponseDto;
import com.betacom.betacom.entity.User;
import com.betacom.betacom.exception.UserAlreadyExistsException;
import com.betacom.betacom.exception.UserNotFoundException;
import com.betacom.betacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return new UserResponseDto(user.getId(), user.getLogin(), user.getCreatedAt());
    }

    public void registerUser(UserRegistrationDto userRegistrationDto) {
        if (userRepository.existsByLogin(userRegistrationDto.getLogin())) {
            throw new UserAlreadyExistsException();
        }
        User user = User.builder()
                .login(userRegistrationDto.getLogin())
                .password(passwordEncoder.encode(userRegistrationDto.getPassword()))
                .build();

        userRepository.save(user);
    }
}
