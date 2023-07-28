package com.alibou.service;


import com.alibou.demo.FileService;
import com.alibou.entities.Attachment;
import com.alibou.entities.Enums.Role;
import com.alibou.entities.User;
import com.alibou.payload.*;
import com.alibou.repository.AttachmentRepository;
import com.alibou.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


@Service
@RequiredArgsConstructor

public class UserService {
    private final PasswordEncoder passwordEncoder;
    final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final FileService fileService;
    final AttachmentRepository attachmentRepository;
    public static final Path root = Paths.get(System.getProperty("user.dir"), "files");

    public ApiResponse profile(User user) {
        Optional<User> byId = userRepository.findById(user.getId());
        if (byId.isPresent()) {
            UserRes userRes = new UserRes();
            userRes.setEmail(user.getEmail());
            userRes.setLastname(user.getLastname());
            userRes.setPhoneNumber(user.getPhoneNumber());
            userRes.setFirstname(user.getFirstname());
            if (user.getAttachment() != null) {
                userRes.setAttachmentId(user.getAttachment().getId());
            }
            return new ApiResponse(true, userRes);
        }
        return null;
    }

    public ApiResponse editPassword(User user, OldAndNewPasswords passwords) {
        if (passwordEncoder.matches(passwords.getOldPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(passwords.getNewPassword()));
            userRepository.save(user);
            return new ApiResponse("Пароль изменен", true);
        }
        return new ApiResponse("Старый пароль введен неверно", false);
    }

    public ApiResponse updateUserInfo(Integer userId, String firstname, String lastname, String phoneNumber, Date dateOfBirth) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (firstname != null) {
                user.setFirstname(firstname);
            }
            if (lastname != null) {
                user.setLastname(lastname);
            }
            if (phoneNumber != null) {
                if (!userRepository.existsByPhoneNumber(phoneNumber)) {
                    user.setPhoneNumber(phoneNumber);
                }
            }
            if (dateOfBirth != null) {
                user.setDateOfBirth(dateOfBirth);
            }
            userRepository.save(user);
            return new ApiResponse("Изменение применены", true);
        } else {
            return new ApiResponse("Пользователь не найден", true);
        }
    }


    public ApiResponse forgotMyPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceAccessException("User not found"));
        String random = String.valueOf(generateRandom(6));
        setVerificationPasswordWithExpiry(user, random);
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("no-reply@mail.upgradeeducation.uz");
        simpleMailMessage.setTo(user.getEmail());
        simpleMailMessage.setSubject("Забыл пароль");
        simpleMailMessage.setText(
                "Ваш аккаунт: " + email +
                        "\n" + "Ваш код подтверждения для смены пароля:" +
                        random);
        javaMailSender.send(simpleMailMessage);
        return new ApiResponse("Отправка кода подтвердения на вашу почту", true);


    }

    public ApiResponse verify(EmailVerifyPasswordReq emailVerifyPasswordReq) {
        User user = userRepository.findByEmail(emailVerifyPasswordReq.getEmail())
                .orElseThrow(() -> new ResourceAccessException("User not found"));
        if (isVerificationPasswordValid(user) != false) {
            if (user.getVerificationPassword().equals(emailVerifyPasswordReq.getEmailCode())) {
                if (!user.isEnabled()) {
                    user.setEnabled(true);
                }
                user.setPassword(passwordEncoder.encode(emailVerifyPasswordReq.getNewPassword()));
                user.setVerificationPassword(null);
                user.setVerificationPasswordExpiry(null);
                userRepository.save(user);
                return new ApiResponse("Пароль изменен", true);
            }
            return new ApiResponse("Код подтверждения введен неверно", false);
        }
        return new ApiResponse("Срок кода истек", false);
    }


    public ApiResponse editAvatar(MultipartFile file, User user) throws IOException {
        Optional<User> byId = userRepository.findById(user.getId());
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
                    userRepository.save(byId.get());
                    return new ApiResponse("Аватарка добавился", true);
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
                    userRepository.save(byId.get());
                    return new ApiResponse("Аватарка изменился", true);
                }
            } else {
                if (byId.get().getAttachment() != null) {
                    fileService.deleteFile(byId.get().getAttachment().getFilePath());
                    Attachment attachment = byId.get().getAttachment();
                    byId.get().setAttachment(null);
                    userRepository.save(byId.get());
                    attachmentRepository.delete(attachment);
                    return new ApiResponse("Аватарка удалился", true);
                }
            }
        }
        return new ApiResponse("Такого юзера не существует", true);
    }


    public ApiResponse getAllUsers() {
        List<UserResAdmin> usersResAdmin = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserResAdmin userResAdmin = new UserResAdmin();
            userResAdmin.setId(user.getId());
            userResAdmin.setEmail(user.getEmail());
            userResAdmin.setFirstname(user.getFirstname());
            userResAdmin.setLastname(user.getFirstname());
            userResAdmin.setPhoneNumber(user.getPhoneNumber());
            userResAdmin.setDateOfBirth(user.getDateOfBirth());
            userResAdmin.setRegistrationDate(user.getRegistrationDate());
            userResAdmin.setRegistrationDate(userResAdmin.getRegistrationDate());
            if (user.getRole().toString().equals("CLIENT")) {
                userResAdmin.setCoursePaid(true);
            } else {
                userResAdmin.setCoursePaid(false);
            }
            usersResAdmin.add(userResAdmin);
        }
        return new ApiResponse(true, usersResAdmin);
    }

    public ApiResponse getPaid(User user) {
        User user1 = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceAccessException("User not found"));
        if (user1.getRole().toString().equals("CLIENT")) {
            return new ApiResponse("Этот пользователь покупал курс", true);
        } else {
            return new ApiResponse("Этот пользователь не покупал курс", false);
        }
    }

    public ApiResponse pay(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceAccessException("User not found"));
        if (user.getRole() == Role.CLIENT) {
            user.setRole(Role.USER);
            userRepository.save(user);
            return new ApiResponse("Пользователь выключен", true);
        } else if (user.getRole() == Role.USER) {
            user.setRole(Role.CLIENT);
            userRepository.save(user);
            return new ApiResponse("Пользователь включен", true);
        }
        return null;
    }

    public int generateRandom(int integerLength) {
        Random random = new Random();
        int min = (int) Math.pow(10, integerLength - 1);
        int max = (int) Math.pow(10, integerLength) - 1;
        return random.nextInt(max - min + 1) + min;
    }

    public void setVerificationPasswordWithExpiry(User user, String verificationPassword) {
        user.setVerificationPassword(verificationPassword);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 10);
        user.setVerificationPasswordExpiry(calendar.getTime());
        userRepository.save(user);
    }

    // Метод для проверки, действителен ли verificationPassword в текущий момент
    public boolean isVerificationPasswordValid(User user) {
        if (user.getVerificationPasswordExpiry() == null) {
            return false;
        }
        return new Date().before(user.getVerificationPasswordExpiry());
    }
}

