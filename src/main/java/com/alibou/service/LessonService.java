package com.alibou.service;

import com.alibou.demo.FileService;
import com.alibou.entities.Attachment;
import com.alibou.entities.Lesson;
import com.alibou.entities.Module;

import com.alibou.entities.User;
import com.alibou.payload.*;
import com.alibou.repository.AttachmentRepository;
import com.alibou.repository.LessonRepository;
import com.alibou.repository.ModuleRepository;
import com.alibou.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class LessonService {
    final LessonRepository lessonRepository;
    final ModuleRepository moduleRepository;
    final AttachmentRepository attachmentRepository;
    final UserRepository userRepository;
    private final FileService fileService;

    public static final Path root = Paths.get(System.getProperty("user.dir"), "files");

    public ApiResponse addLesson(LessonReq lessonReq) {
        Optional<Module> byId = moduleRepository.findById(lessonReq.getModuleId());
        if (byId.isPresent()) {
            Lesson lesson = new Lesson();
            lesson.setName(lessonReq.getName());
            lesson.setModule(byId.get());
            lesson.setLessonNumber(lessonReq.getLessonNumber());
            lesson.setDescription(lessonReq.getDescription());
            lesson.setHomework(lessonReq.getHomework());
            Lesson save = lessonRepository.save(lesson);
            byId.get().addLesson(save);
            moduleRepository.save(byId.get());
            return new ApiResponse("Success", true);
        }
        return new ApiResponse("Модуль не существует", false);
    }

    public ApiResponse addBonuses(Long id, MultipartFile file1, MultipartFile file2, MultipartFile file3) throws IOException {
        Optional<Lesson> optionalLesson = lessonRepository.findById(id);

        if (optionalLesson.isPresent()) {
            Lesson lesson = optionalLesson.get();
            if (!Files.exists(root)) {
                Files.createDirectory(root);
                Files.setAttribute(root, "dos:hidden", true);
            }

            // Обработка файла file1
            handleFile(file1, lesson.getBonus1(), root, lesson, attachmentRepository, fileService);

            // Обработка файла file2
            handleFile(file2, lesson.getBonus2(), root, lesson, attachmentRepository, fileService);

            // Обработка файла file3
            handleFile(file3, lesson.getBonus3(), root, lesson, attachmentRepository, fileService);

            return new ApiResponse("Бонусы добавлены или удалены", true);
        } else {
            return new ApiResponse("Урок не найден", false);
        }
    }

    private void handleFile(MultipartFile file, Attachment bonus, Path root, Lesson lesson, AttachmentRepository attachmentRepository, FileService fileService) throws IOException {
        if (file != null && file.getSize() > 0) {
            String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), root.resolve(uniqueFilename));

            if (bonus != null) {
                fileService.deleteFile(bonus.getFilePath());
                bonus.setFilePath(root + "\\" + uniqueFilename);
                bonus.setName(uniqueFilename);
                bonus.setContentType(file.getContentType());
                bonus.setSize(file.getSize());
                Attachment savedAttachment = attachmentRepository.save(bonus);
                if (bonus.equals(lesson.getBonus1())) {
                    lesson.setBonus1(savedAttachment);
                } else if (bonus.equals(lesson.getBonus2())) {
                    lesson.setBonus2(savedAttachment);
                } else {
                    lesson.setBonus3(savedAttachment);
                }
            } else {
                Attachment newAttachment = new Attachment();
                newAttachment.setFilePath(root + "\\" + uniqueFilename);
                newAttachment.setName(uniqueFilename);
                newAttachment.setContentType(file.getContentType());
                newAttachment.setSize(file.getSize());
                Attachment savedAttachment = attachmentRepository.save(newAttachment);
                if (lesson.getBonus1() == null) {
                    lesson.setBonus1(savedAttachment);
                } else if (lesson.getBonus2() == null) {
                    lesson.setBonus2(savedAttachment);
                } else {
                    lesson.setBonus3(savedAttachment);
                }
            }
            lessonRepository.save(lesson);
        } else {
            if (bonus != null) {
                UUID uuid = bonus.getId();
                fileService.deleteFile(bonus.getFilePath());
                if (bonus.equals(lesson.getBonus1())) {
                    lesson.setBonus1(null);
                } else if (bonus.equals(lesson.getBonus2())) {
                    lesson.setBonus2(null);
                } else {
                    lesson.setBonus3(null);
                }
                lessonRepository.save(lesson);
                attachmentRepository.deleteById(uuid);
            }
        }
    }


    public ApiResponse setLesson(Long id, LessonReq lessonReq) {
        Optional<Lesson> byId = lessonRepository.findById(id);
        if (byId.isPresent()) {
            byId.get().setLessonNumber(lessonReq.getLessonNumber());
            byId.get().setName(lessonReq.getName());
            byId.get().setHomework(lessonReq.getHomework());
            byId.get().setDescription(lessonReq.getDescription());
            lessonRepository.save(byId.get());
            return new ApiResponse("Изменено", true);
        }
        return new ApiResponse("Урока не существует", false);

    }


    public ApiResponse addVideoToLesson(Long id, MultipartFile file) throws IOException {
        Optional<Lesson> byId = lessonRepository.findById(id);
        if (byId.isPresent()) {
            if (file.getSize() != 0) {
                String uniqueFilename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), root.resolve(uniqueFilename));
                if (byId.get().getVideo() == null) {
                    Attachment newAttachment = new Attachment();
                    newAttachment.setFilePath(root + "\\" + uniqueFilename);
                    newAttachment.setName(uniqueFilename);
                    newAttachment.setContentType(file.getContentType());
                    newAttachment.setSize(file.getSize());
                    Attachment save = attachmentRepository.save(newAttachment);
                    byId.get().setVideo(save);
                    lessonRepository.save(byId.get());
                    return new ApiResponse("Видео добавился", true);
                } else {
                    Attachment attachment = byId.get().getVideo();
                    String filePath = attachment.getFilePath();
                    fileService.deleteFile(filePath);
                    attachment.setFilePath(root + "\\" + uniqueFilename);
                    attachment.setName(uniqueFilename);
                    attachment.setContentType(file.getContentType());
                    attachment.setSize(file.getSize());
                    Attachment save = attachmentRepository.save(attachment);
                    byId.get().setVideo(save);
                    lessonRepository.save(byId.get());
                    return new ApiResponse("Видео изменился", true);
                }
            } else {
                if (byId.get().getVideo() != null) {
                    fileService.deleteFile(byId.get().getVideo().getFilePath());
                    Attachment attachment = byId.get().getVideo();
                    byId.get().setVideo(null);
                    lessonRepository.save(byId.get());
                    attachmentRepository.delete(attachment);
                    return new ApiResponse("Видео удалился", true);
                }
            }
        }
        return new ApiResponse("Такого урока не существует", true);
    }

    public ApiResponse getOne(Long id) {
        Optional<Lesson> lesson = lessonRepository.findById(id);
        if (lesson.isPresent()) {
            LessonRes lesson1 = new LessonRes();
            if (lesson.get().getVideo() != null) {
                lesson1.setVideoId(UUID.fromString(lesson.get().getVideo().getId().toString()));
            }
            lesson1.setId(lesson.get().getId());
            lesson1.setName(lesson.get().getName());
            lesson1.setLessonNumber(lesson.get().getLessonNumber());
            lesson1.setModuleId(lesson.get().getModule().getId());
            lesson1.setDescription(lesson.get().getDescription());
            lesson1.setHomework(lesson.get().getHomework());
            if (lesson.get().getBonus1() != null) {
                lesson1.setBonus1(lesson.get().getBonus1().getId());
            }
            if (lesson.get().getBonus2() != null) {
                lesson1.setBonus2(lesson.get().getBonus2().getId());
            }
            if (lesson.get().getBonus3() != null) {
                lesson1.setBonus3(lesson.get().getBonus3().getId());
            }

            return new ApiResponse(true, lesson1);
        }
        return new ApiResponse("Урока не доступен", false);
    }


    public ApiResponse delete(Long lessonId) {
        Optional<Lesson> lesson = lessonRepository.findById(lessonId);
        if (lesson.isPresent()) {
            if (lesson.get().getVideo() != null) {
                String filePath = lesson.get().getVideo().getFilePath();
                fileService.deleteFile(filePath);
            }
            if (lesson.get().getBonus1() != null) {
                String filePath = lesson.get().getBonus1().getFilePath();
                fileService.deleteFile(filePath);
            }
            if (lesson.get().getBonus2() != null) {
                String filePath = lesson.get().getBonus2().getFilePath();
                fileService.deleteFile(filePath);
            }
            if (lesson.get().getBonus3() != null) {
                String filePath = lesson.get().getBonus3().getFilePath();
                fileService.deleteFile(filePath);
            }
            List<User> all = userRepository.findAll();
            for (User user : all) {
                user.deleteLesson(lesson.get());
            }
            lesson.get().getModule().getLessons().remove(lesson.get());
            moduleRepository.save(lesson.get().getModule());
            lessonRepository.delete(lesson.get());
            return new ApiResponse("Успешно удалено", true);
        }
        return new ApiResponse("Урока не доступен", false);
    }

    public ApiResponse videoView(Long id, User user) {
        Optional<Lesson> lesson = lessonRepository.findById(id);
        Optional<User> user1 = userRepository.findById(user.getId());
        if (lesson.isPresent()) {
            if (user1.isPresent()) {
                Lesson lesson1 = user1.get().viewLesson(lesson.get());
                lessonRepository.save(lesson1);
                userRepository.save(user);
                return new ApiResponse("Простмотр добавился", true);
            }
            return new ApiResponse("Пользователь не существует", true);
        }
        return new ApiResponse("Урок не существует", true);
    }

    public ApiResponse videoViewPercent(User user) {
        Optional<User> byId = userRepository.findById(user.getId());
        if (lessonRepository.count() != 0) {
            int count = (int) lessonRepository.count();
            int size = byId.get().getViewedLessons().size();
            String result = String.valueOf(size * 100 / count);
            return new ApiResponse(true, result);
        }
        return new ApiResponse(true, "0");
    }

}