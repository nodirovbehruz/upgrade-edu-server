package com.alibou.repository;



import com.alibou.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    void deleteByFilePath(String filePath);
}
