package com.alibou.demo;


import com.alibou.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@PreAuthorize("hasAuthority('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {
    private final UserService userService;

    @GetMapping("/get/all/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/set/user/{id}")
    public ResponseEntity<?> setUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.pay(id));
    }

}
