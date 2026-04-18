package com.dev.transcribeflow.token;

import com.dev.transcribeflow.auth.dto.request.RefreshTokenRequestDTO;
import com.dev.transcribeflow.core.exception.EmailNotFoundException;
import com.dev.transcribeflow.core.exception.ExpireTokenException;
import com.dev.transcribeflow.core.utils.MessageUtils;
import com.dev.transcribeflow.user.UserEntity;
import com.dev.transcribeflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final MessageUtils messageUtils;

    @Value("${security.jwt.refresh-expiration-time}")
    private long refreshTokenDuration;

    public RefreshTokenEntity createRefreshToken(String email){
         log.debug("Generating new Refresh token for the user: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(()-> new EmailNotFoundException(
                        messageUtils.getMessage(
                                "auth.login.error.email_not_found",
                                email)));

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.flush();

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDuration))
                .revoked(false)
                .build();

        RefreshTokenEntity savedToken = refreshTokenRepository.save(refreshToken);
        log.info("Refresh token saved successfully in Database for the user: {}", email);

        return savedToken;
    }

    public Optional<RefreshTokenEntity> findByToken(String token){
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity token){
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            log.warn("The Refresh Token of user {} has expired and was deleted", token.getUser().getEmail());
            throw new ExpireTokenException(messageUtils.getMessage(
                    "auth.token.error.expired"));
        }
        return token;
    }

    @Transactional
    public void deleteByToken(String token){
        log.debug("Attempting to delete refresh token");
        refreshTokenRepository.findByToken(token)
                .ifPresent(tokenEntity -> {
                    refreshTokenRepository.delete(tokenEntity);
                    log.info("Refresh token for user {} deleted successfully", tokenEntity.getUser().getEmail());
                });
    }

}
