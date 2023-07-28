package com.alibou.service;

import com.alibou.demo.FileService;
import com.alibou.entities.Attachment;
import com.alibou.entities.Lesson;
import com.alibou.entities.Module;
import com.alibou.entities.User;
import com.alibou.payload.ApiResponse;
import com.alibou.payload.LessonRes;
import com.alibou.payload.ModuleReq;
import com.alibou.payload.ModuleRes;
import com.alibou.repository.AttachmentRepository;
import com.alibou.repository.LessonRepository;
import com.alibou.repository.ModuleRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
public class ModuleService {
    final ModuleRepository repository;
    final LessonRepository lessonRepository;
    final LessonService lessonService;
    final AttachmentRepository attachmentRepository;
    final FileService fileService;

    public ApiResponse addModule(ModuleReq moduleReq) {
        Module module = new Module();
        module.setModulNumber(moduleReq.getModuleNumber());
        module.setName(moduleReq.getName());
        repository.save(module);
        return new ApiResponse("Success", true);
    }

    public static final Path root = Paths.get(System.getProperty("user.dir"), "files");

    public ApiResponse deleteModule(Integer id) {
        Optional<Module> module = repository.findById(id);
        if (module.isPresent()) {
            List<Lesson> lessonsToDelete = new ArrayList<>(module.get().getLessons());
            for (Lesson lesson : lessonsToDelete) {
                lessonService.delete(lesson.getId());
            }
            if (module.get().getAttachment() != null) {
                fileService.deleteFile(module.get().getAttachment().getFilePath());
            }
            repository.deleteById(id);
            return new ApiResponse("Успешно удалено", true);
        }
        return new ApiResponse("Модуль не существует", false);
    }

    public ApiResponse getAll() {
        List<ModuleRes> moduleRes = new ArrayList<>();
        for (Module module : repository.findAll()) {
            ModuleRes moduleRes1 = new ModuleRes();
            moduleRes1.setId(module.getId());
            moduleRes1.setModuleNumber(module.getId());
            moduleRes1.setName(module.getName());
            if (module.getAttachment() != null) {
                moduleRes1.setIcon(module.getAttachment().getId());
            }
            moduleRes.add(moduleRes1);
            List<LessonRes> lessons = new ArrayList<>();
            for (Lesson lesson : module.getLessons()) {
                LessonRes lessonRes = new LessonRes();
                lessonRes.setId(lesson.getId());
                lessonRes.setLessonNumber(lesson.getLessonNumber());
                lessonRes.setName(lesson.getName());
                lessons.add(lessonRes);
            }
            moduleRes1.setLessons(lessons);
        }
        return new ApiResponse(true, moduleRes);
    }

    public ApiResponse add(MultipartFile file, Integer id) throws IOException {
        Optional<Module> byId = repository.findById(id);
        if (byId.isPresent()) {
            if (file.getSize() != 0) {
                String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), root.resolve(uniqueFilename));
                if (byId.get().getAttachment() == null) {
                    Attachment newAttachment = new Attachment();
                    newAttachment.setFilePath(root + "\\" + uniqueFilename);
                    newAttachment.setName(uniqueFilename);
                    newAttachment.setContentType(file.getContentType());
                    newAttachment.setSize(file.getSize());
                    Attachment save = attachmentRepository.save(newAttachment);
                    byId.get().setAttachment(save);
                    repository.save(byId.get());
                    return new ApiResponse("Фотка добавился", true);
                } else {
                    Attachment attachment = byId.get().getAttachment();
                    String filePath = attachment.getFilePath();
                    fileService.deleteFile(filePath);
                    attachment.setFilePath(root + "\\" + uniqueFilename);
                    attachment.setName(uniqueFilename);
                    attachment.setContentType(file.getContentType());
                    attachment.setSize(file.getSize());
                    Attachment save = attachmentRepository.save(attachment);
                    byId.get().setAttachment(save);
                    repository.save(byId.get());
                    return new ApiResponse("Фотка изменился", true);
                }
            } else {
                if (byId.get().getAttachment() != null) {
                    fileService.deleteFile(byId.get().getAttachment().getFilePath());
                    Attachment attachment = byId.get().getAttachment();
                    byId.get().setAttachment(null);
                    repository.save(byId.get());
                    attachmentRepository.delete(attachment);
                    return new ApiResponse("Фотка удалился", true);
                }
            }
        }
        return new ApiResponse("Такого модуля не существует", true);
    }


    public ApiResponse editModule(Integer id, ModuleReq req) {
        Optional<Module> module = repository.findById(id);
        if (module.isPresent()) {
            Module module1 = new Module();
            module1.setId(module.get().getId());
            module1.setLessons(module.get().getLessons());
            module1.setModulNumber(req.getModuleNumber());
            module1.setName(req.getName());
            repository.save(module1);
            return new ApiResponse("Успешно изменен", true);
        }
        return new ApiResponse("Модуль не существует", false);
    }
}
