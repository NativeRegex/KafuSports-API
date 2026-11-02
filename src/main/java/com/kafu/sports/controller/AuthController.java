package com.kafu.sports.controller;

import com.kafu.sports.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // For now, hard-coded admin credentials (same as before)
    // In production, move this to a User entity with BCrypt-hashed passwords
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "kafu2026";
    private static final String ADMIN_ROLE = "ADMIN";

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            String token = jwtUtil.generateToken(username, ADMIN_ROLE);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "user", Map.of("username", username, "role", ADMIN_ROLE)
            ));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "No token"));
        }
        String token = auth.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }
        return ResponseEntity.ok(Map.of(
                "username", jwtUtil.extractUsername(token),
                "role", jwtUtil.extractRole(token)
        ));
    }
}
