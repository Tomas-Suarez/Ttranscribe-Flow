package com.dev.transcribeflow.auth;

import com.dev.transcribeflow.auth.dto.request.RegisterRequestDTO;
import com.dev.transcribeflow.auth.dto.response.AuthResponseDTO;

public interface IAuthService {

    AuthResponseDTO register(RegisterRequestDTO registerRequestDTO);
}
