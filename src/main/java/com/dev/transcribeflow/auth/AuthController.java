package com.dev.transcribeflow.auth;

import com.dev.transcribeflow.auth.dto.request.LoginRequestDTO;
import com.dev.transcribeflow.auth.dto.request.RefreshTokenRequestDTO;
import com.dev.transcribeflow.auth.dto.request.RegisterRequestDTO;
import com.dev.transcribeflow.auth.dto.response.AuthResponseDTO;
import com.dev.transcribeflow.auth.dto.response.LoginResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequestDTO){
        AuthResponseDTO authResponseDTO = authService.register(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO){
        return ResponseEntity.ok(authService.login(requestDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        return ResponseEntity.ok(authService.refreshToken(refreshTokenRequestDTO));
    };

    @PostMapping("logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        authService.logout(refreshTokenRequestDTO);
        return ResponseEntity.noContent().build();
    }

}
