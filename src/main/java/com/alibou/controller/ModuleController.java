package com.alibou.controller;

import com.alibou.entities.User;
import com.alibou.security.CurrentUser;
import com.alibou.service.ModuleService;
import com.alibou.payload.ApiResponse;
import com.alibou.payload.ModuleReq;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RequestMapping("/api/module")
@RestController

public class ModuleController {
    final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add")
    public HttpEntity<?> addModule(@RequestBody ModuleReq req) {
        return ResponseEntity.ok(moduleService.addModule(req));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ApiResponse delete(@PathVariable Integer id) {
        ApiResponse verify = moduleService.deleteModule(id);
        return ResponseEntity.status(verify.isSuccess() ? 200 : 409).body(verify).getBody();
    }

    @GetMapping("/get/all")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(moduleService.getAll());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/edit/{id}")
    public ApiResponse edit(@PathVariable Integer id, @RequestBody ModuleReq req) {
        ApiResponse verify = moduleService.editModule(id, req);
        return ResponseEntity.status(verify.isSuccess() ? 200 : 409).body(verify).getBody();
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add/photo/{id}")
    public ApiResponse edit(@RequestParam("file") MultipartFile file, @PathVariable Integer id) throws IOException {
        ApiResponse verify = moduleService.add(file,id);
        return ResponseEntity.status(verify.isSuccess() ? 200 : 409).body(verify).getBody();
    }

}
