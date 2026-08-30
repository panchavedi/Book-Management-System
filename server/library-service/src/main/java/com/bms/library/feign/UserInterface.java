package com.bms.library.feign;

import com.bms.library.dto.TokenValidationResponse;
import com.bms.library.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "user-service",
        url = "${user-service.url:http://localhost:8081}",
        configuration = UserFeignConfiguration.class
)
public interface UserInterface {

    @GetMapping("auth/validate")
    ResponseEntity<TokenValidationResponse> validate(
            @RequestParam String token
    );

    @GetMapping("/user/{id}")
    ResponseEntity<UserResponse> getUserById(
            @PathVariable("id") Long id
    );
}
