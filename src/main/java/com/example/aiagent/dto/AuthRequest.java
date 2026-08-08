package com.example.aiagent.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRequest {

    Long id;
    String username;
    String password;
}
