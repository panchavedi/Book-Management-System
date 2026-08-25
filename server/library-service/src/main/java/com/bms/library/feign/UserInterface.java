package com.bms.library.feign;

import com.bms.library.dto.TokenValidationResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("USERSERVICE")
public interface UserInterface {

//    @PostMapping("/api/auth/register")
//    public ResponseEntity<RegisterResponse> register(
//            @Valid @RequestBody RegisterRequest request);
//
//    @PostMapping("/api/auth/login")
//    public ResponseEntity<LoginResponse> login(
//            @Valid @RequestBody LoginRequest request);

    @GetMapping("/api/auth/validate")
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestParam String token);
}
