package com.wasteai.controller;

import com.wasteai.dto.AdviceRequest;
import com.wasteai.dto.AdviceResponse;
import com.wasteai.dto.ChatRequest;
import com.wasteai.dto.ChatResponse;
import com.wasteai.dto.SessionResponse;
import com.wasteai.service.AssistantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/advice")
    public ResponseEntity<AdviceResponse> advice(@Valid @RequestBody AdviceRequest request) {
        return ResponseEntity.ok(assistantService.generateAdvice(request));
    }

    @PostMapping("/chat/{sessionId}")
    public ResponseEntity<ChatResponse> chat(@PathVariable String sessionId, @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(assistantService.chat(sessionId, request));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<SessionResponse> session(@PathVariable String sessionId) {
        return ResponseEntity.ok(assistantService.getSession(sessionId));
    }
}
