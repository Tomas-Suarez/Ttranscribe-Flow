package com.dev.transcribeflow.auth;

import com.dev.transcribeflow.auth.dto.request.LoginRequestDTO;
import com.dev.transcribeflow.auth.dto.request.RefreshTokenRequestDTO;
import com.dev.transcribeflow.auth.dto.request.RegisterRequestDTO;
import com.dev.transcribeflow.auth.dto.response.AuthResponseDTO;
import com.dev.transcribeflow.auth.dto.response.LoginResponseDTO;
import com.dev.transcribeflow.auth.mapper.UserMapper;
import com.dev.transcribeflow.core.exception.EmailAlreadyExistsException;
import com.dev.transcribeflow.core.exception.EmailNotFoundException;
import com.dev.transcribeflow.core.exception.TokenNotFoundException;
import com.dev.transcribeflow.core.security.principal.SecurityUser;
import com.dev.transcribeflow.core.security.service.JwtService;
import com.dev.transcribeflow.core.utils.MessageUtils;
import com.dev.transcribeflow.subscription.SubscriptionService;
import com.dev.transcribeflow.token.RefreshTokenEntity;
import com.dev.transcribeflow.token.RefreshTokenService;
import com.dev.transcribeflow.user.Role;
import com.dev.transcribeflow.user.UserEntity;
import com.dev.transcribeflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageUtils messageUtils;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final SubscriptionService subscriptionService;

    public AuthResponseDTO register(RegisterRequestDTO registerRequestDTO){
        log.debug("Registering: {}", registerRequestDTO.email());

        if(userRepository.existsByEmail(registerRequestDTO.email())){
            throw new EmailAlreadyExistsException(messageUtils.getMessage(
                    "auth.register.error.email_exists",
                    registerRequestDTO.email()));
        }

        UserEntity user = userMapper.toEntity(registerRequestDTO);

        user.setPassword(passwordEncoder.encode(registerRequestDTO.password()));
        user.setRole(Role.USER);
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);

        log.info("User registered successfully with ID: {}", savedUser.getId());

        subscriptionService.createDefaultSubscription(savedUser.getId());

        return userMapper.toResponse(savedUser, messageUtils.getMessage("auth.register.success"));
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.email(), loginRequestDTO.password())
        );

        UserEntity user = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(()-> new EmailNotFoundException(messageUtils.getMessage(
                        "auth.login.error.email_not_found",
                        loginRequestDTO.email())));

        String accessToken = jwtService.generateToken(new SecurityUser(user));
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return new LoginResponseDTO(
                accessToken,
                refreshToken.getToken(),
                user.getEmail(),
                messageUtils.getMessage("auth.login.success")
        );
    }

    public void logout(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        refreshTokenService.deleteByToken(refreshTokenRequestDTO.token());
        log.info("Logout successful: Refresh token invalidated.");
    }

    @Transactional
    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return refreshTokenService.findByToken(refreshTokenRequestDTO.token())
                .map(refreshTokenService::verifyExpiration)
                .map(tokenEntity -> {
                    UserEntity user = tokenEntity.getUser();

                    refreshTokenService.deleteByToken(tokenEntity.getToken());

                    String newAccessToken = jwtService.generateToken(new SecurityUser(user));
                    RefreshTokenEntity newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

                    return new LoginResponseDTO(
                            newAccessToken,
                            newRefreshToken.getToken(),
                            user.getEmail(),
                            messageUtils.getMessage("auth.token.refresh.success")
                    );
                })
                .orElseThrow(() -> new TokenNotFoundException(
                        messageUtils.getMessage("auth.token.error.not_found")
                ));
    }

}
