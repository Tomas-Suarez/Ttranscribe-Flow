package com.dev.transcribeflow.auth;

import com.dev.transcribeflow.auth.dto.request.RegisterRequestDTO;
import com.dev.transcribeflow.auth.dto.response.AuthResponseDTO;
import com.dev.transcribeflow.auth.mapper.UserMapper;
import com.dev.transcribeflow.core.exception.EmailAlreadyExistsException;
import com.dev.transcribeflow.core.utils.MessageUtils;
import com.dev.transcribeflow.user.Role;
import com.dev.transcribeflow.user.UserEntity;
import com.dev.transcribeflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageUtils messageUtils;

    public AuthResponseDTO register(RegisterRequestDTO registerRequestDTO){
        log.info("Registering: {}", registerRequestDTO.email());

        if(userRepository.existsByEmail(registerRequestDTO.email())){
            throw new EmailAlreadyExistsException(messageUtils.getMessage(
                    "auth.register.error.email_exists",
                    registerRequestDTO.email()));
        }

        UserEntity user = userMapper.toEntity(registerRequestDTO);

        user.setPassword(passwordEncoder.encode(registerRequestDTO.password()));
        user.setRole(Role.USER);

        UserEntity savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser, messageUtils.getMessage("auth.register.success"));
    }

}
