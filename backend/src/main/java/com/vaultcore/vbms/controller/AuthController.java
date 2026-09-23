package com.vaultcore.vbms.controller;
import com.vaultcore.vbms.dto.AuthDtos.*;
import com.vaultcore.vbms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/auth")
public class AuthController { private final AuthService service; public AuthController(AuthService s){service=s;} @PostMapping("/register") ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest r){service.register(r);return ResponseEntity.status(HttpStatus.CREATED).build();} @PostMapping("/login") LoginResponse login(@Valid @RequestBody LoginRequest r){return service.login(r);} }
