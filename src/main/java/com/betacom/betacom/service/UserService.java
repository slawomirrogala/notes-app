package com.betacom.betacom.service;

import com.betacom.betacom.dto.UserRegistrationDto;
import com.betacom.betacom.dto.UserResponseDto;
import com.betacom.betacom.entity.User;
import com.betacom.betacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));
        return new UserResponseDto(user.getId(), user.getLogin(), user.getCreatedAt());
    }

    public void registerUser(UserRegistrationDto userRegistrationDto) {
        User user = User.builder()
                .login(userRegistrationDto.getLogin())
                .password(userRegistrationDto.getPassword())
                .build();

        userRepository.save(user);
    }
}
