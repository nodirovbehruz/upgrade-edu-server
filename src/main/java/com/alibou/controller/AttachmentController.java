package com.alibou.controller;

import com.alibou.payload.AttachmentResDto;
import com.alibou.service.AttachmentService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;

@RestController
@RequestMapping("/attachment")
public class AttachmentController {
    final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("/upload")
    public UUID upload(@RequestParam("file") MultipartFile file) throws IOException {
        return attachmentService.upload(file);
    }

    @GetMapping("/getFile/{id}")
    public HttpEntity<?> download(@PathVariable UUID id) throws MalformedURLException {
        AttachmentResDto file = attachmentService.getFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(file.getAttachment().getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getAttachment().getName())
                .body(file.getResource());
    }
}
