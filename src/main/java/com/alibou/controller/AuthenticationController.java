package com.alibou.controller;

import com.alibou.auth.AuthenticationRequest;
import com.alibou.auth.AuthenticationResponse;
import com.alibou.payload.EmailVerifyPasswordReq;
import com.alibou.service.AuthenticationService;
import com.alibou.auth.RegisterRequest;
import com.alibou.payload.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        ApiResponse register = service.register(request);
        return ResponseEntity.status(register.isSuccess() ? 200 : 409).body(register);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }


    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody EmailVerifyPasswordReq emailVerifyPasswordReq) {
        ApiResponse verify = service.verify(emailVerifyPasswordReq);
        return ResponseEntity.status(verify.isSuccess() ? 200 : 409).body(verify);
    }
    @PostMapping("/send/again")
    @ResponseBody
    public ResponseEntity<ApiResponse> sendAgain(@RequestBody EmailVerifyPasswordReq emailVerifyPasswordReq) {
        ApiResponse verify = service.sendEmailAgain(emailVerifyPasswordReq.getEmail());
        return ResponseEntity.status(verify.isSuccess() ? 200 : 409).body(verify);
    }

}
