package com.alibou.controller;

import com.alibou.entities.User;
import com.alibou.payload.ApiResponse;
import com.alibou.payload.LessonReq;
import com.alibou.security.CurrentUser;
import com.alibou.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/lesson")
public class LessonController {
    final LessonService lessonService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add")
    public HttpEntity<?> addLesson(@RequestBody LessonReq req) {
        return ResponseEntity.ok(lessonService.addLesson(req));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/upload/bonuses/{id}")
    public ResponseEntity<?> uploadBonuses(@RequestParam("file1") MultipartFile file1, @RequestParam("file2") MultipartFile file2,
                                           @RequestParam("file3") MultipartFile file3, @PathVariable Long id) throws IOException {
        ApiResponse upload = lessonService.addBonuses(id, file1, file2, file3);
        return ResponseEntity.status(upload.isSuccess() ? 200 : 409).body(upload);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/upload/{id}")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, @PathVariable Long id) throws IOException {
        ApiResponse upload = lessonService.addVideoToLesson(id, file);
        return ResponseEntity.status(upload.isSuccess() ? 200 : 409).body(upload);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CLIENT')")
    @GetMapping("/get/one/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        ApiResponse upload = lessonService.getOne(id);
        return ResponseEntity.status(upload.isSuccess() ? 200 : 409).body(upload);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable Long id) {
        ApiResponse upload = lessonService.delete(id);
        return ResponseEntity.status(upload.isSuccess() ? 200 : 409).body(upload);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CLIENT')")
    @PostMapping("/view/{id}")
    public ResponseEntity<?> addView(@PathVariable Long id, @CurrentUser User user) {
        ApiResponse upload = lessonService.videoView(id, user);
        return ResponseEntity.status(upload.isSuccess() ? 200 : 409).body(upload);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CLIENT')")
    @GetMapping("/view/get")
    public ResponseEntity<?> getView(@CurrentUser User user) {
        ApiResponse upload = lessonService.videoViewPercent(user);
        return ResponseEntity.status(upload.isSuccess() ? 200 : 409).body(upload);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/set/lesson/{id}")
    public ResponseEntity<?> setLesson(@PathVariable Long id, @RequestBody LessonReq lessonReq) {
        ApiResponse upload = lessonService.setLesson(id, lessonReq);
        return ResponseEntity.status(upload.isSuccess() ? 200 : 409).body(upload);
    }


}
