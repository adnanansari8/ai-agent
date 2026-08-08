package com.example.aiagent.controller;

import com.example.aiagent.dto.AgentRequest;
import com.example.aiagent.dto.AgentResponse;
import com.example.aiagent.dto.AuthRequest;
import com.example.aiagent.service.AiAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AiAgentController {

    private final AiAgentService agentService;

    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(@RequestBody AgentRequest request) {
        AgentResponse response = agentService.processRequest(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI Agent is running");
    }





}