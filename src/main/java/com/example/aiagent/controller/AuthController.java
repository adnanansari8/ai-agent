package com.example.aiagent.controller;

import com.example.aiagent.dto.AuthRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/auth")
public class AuthController {


    @PostMapping("/auth")
    public ResponseEntity<String> auth(@RequestBody AuthRequest request) {
        return ResponseEntity.ok("Authentication successful");
    }
}
