package com.alibou.controller;


import com.alibou.auth.RegisterRequest;
import com.alibou.entities.User;


import com.alibou.payload.ApiResponse;
import com.alibou.payload.EmailVerifyPasswordReq;
import com.alibou.payload.OldAndNewPasswords;
import com.alibou.security.CurrentUser;
import com.alibou.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@CurrentUser User user) {
        return ResponseEntity.ok(userService.profile(user));
    }

    @PostMapping("/edit/password")
    public ResponseEntity<?> editPassword(@CurrentUser User user, @RequestBody OldAndNewPasswords oldAndNewPasswords) {
        return ResponseEntity.ok(userService.editPassword(user, oldAndNewPasswords));
    }

    @PostMapping("/forgot/password")
    public HttpEntity<?> forgotMyPassword(@RequestBody EmailVerifyPasswordReq emailVerifyPasswordReq) {
        return ResponseEntity.ok(userService.forgotMyPassword(emailVerifyPasswordReq.getEmail()));
    }

    @PostMapping("/forgot/verify")
    public ResponseEntity<?> verifyEmail(@RequestBody EmailVerifyPasswordReq emailVerifyPasswordReq) {
        return ResponseEntity.ok(userService.verify(emailVerifyPasswordReq));
    }
    @GetMapping("/get/role")
    public ResponseEntity<?> getRole(@CurrentUser User user) {
        return ResponseEntity.ok(userService.getPaid(user));
    }
    @PostMapping("/avatar")
    public ResponseEntity<?> addAvatar(@RequestParam("file") MultipartFile file,@CurrentUser User user) throws IOException {
        return ResponseEntity.ok(userService.editAvatar(file,user));
    }
    @PostMapping("/update")
    public ResponseEntity<?> updateUserInfo(@CurrentUser User user ,@RequestBody RegisterRequest request){
        return ResponseEntity.ok(userService.updateUserInfo(user.getId(),request.getFirstname(), request.getLastname(), request.getPhoneNumber(), request.getDateOfBirth()));
    }
    @GetMapping("/get/icon")
    public ResponseEntity<?> getIcon() {
        return ResponseEntity.ok(userService.getIconMenu());
    }

}


