package com.alibou.demo;

import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileService {
    public void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
}