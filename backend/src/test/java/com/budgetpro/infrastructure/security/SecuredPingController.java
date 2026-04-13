package com.budgetpro.infrastructure.security;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecuredPingController {
    @GetMapping("/__secured/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}

