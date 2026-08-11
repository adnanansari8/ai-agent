package com.example.aiagent.controller;

import com.example.aiagent.auth.UserDetailsServiceImpl;
import com.example.aiagent.dto.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("api/auth")
public class AuthController {


    private UserDetailsServiceImpl userDetailsServiceImpl;

    public AuthController(UserDetailsServiceImpl userDetailsServiceImpl)
    {
        this.userDetailsServiceImpl=userDetailsServiceImpl;
    }

    @PostMapping("/token")
    public ResponseEntity<String> auth(@RequestBody AuthRequest request) {

        userDetailsServiceImpl.loadUserByUsername(request.getUsername());
        return ResponseEntity.ok("Authentication successful");
    }
}
