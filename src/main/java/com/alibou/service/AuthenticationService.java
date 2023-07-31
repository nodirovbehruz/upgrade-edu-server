package com.alibou.service;

import com.alibou.auth.AuthenticationRequest;
import com.alibou.auth.AuthenticationResponse;
import com.alibou.auth.RegisterRequest;
import com.alibou.config.JwtService;
import com.alibou.payload.ApiResponse;
import com.alibou.payload.EmailVerifyPasswordReq;
import com.alibou.token.Token;
import com.alibou.repository.TokenRepository;
import com.alibou.token.TokenType;
import com.alibou.entities.User;
import com.alibou.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JavaMailSender javaMailSender;
    private final AuthenticationManager authenticationManager;

    public ApiResponse register(RegisterRequest request) {
        if (!repository.existsByEmail(request.getEmail())) {
            if (!repository.existsByPhoneNumber(request.getPhoneNumber())) {
                var user = User.builder()
                        .firstname(request.getFirstname())
                        .lastname(request.getLastname())
                        .email(request.getEmail())
                        .phoneNumber(request.getPhoneNumber())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .dateOfBirth(request.getDateOfBirth())
                        .role(request.getRole())
                        .build();
                var savedUser = repository.save(user);

                String verificationCode = String.valueOf(generateRandom(6));
                setVerificationPasswordWithExpiry(savedUser, verificationCode);

                SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
                simpleMailMessage.setFrom("no-reply@mail.upgradeeducation.uz");
                simpleMailMessage.setTo(request.getEmail());
                simpleMailMessage.setSubject("Подтверждение аккаунта");
                simpleMailMessage.setText("Ваш аккаунт: " + request.getEmail() +
                        "\n" + "Ваш код подтверждения: " + verificationCode);
                javaMailSender.send(simpleMailMessage);

                return new ApiResponse("Мы отправили вам письмо с кодом подтверждения на вашу почту", true);
            }
            return new ApiResponse("Пользователь с таким номером уже существует", false);
        }
        return new ApiResponse("Пользователь с такой почтой уже существует", false);
    }

    public AuthenticationResponse regadmin(RegisterRequest request) {
        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();

    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceAccessException("User not found"));
        if (user.isEnabled()) {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            request.getPassword()
                    )
            );
            var jwtToken = jwtService.generateToken(user);
            tokenRepository.deleteByUserId(user.getId());
            saveUserToken(user, jwtToken);
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .build();
        }
        return AuthenticationResponse.builder()
                .token("Сначала подтвердите свой аккаунт")
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }




    public ApiResponse verify(EmailVerifyPasswordReq emailVerifyPasswordReq) {
        Optional<User> byEmail = repository.findByEmail(emailVerifyPasswordReq.getEmail());
        if (byEmail.isPresent()) {
            boolean verificationPasswordValid = isVerificationPasswordValid(byEmail.get());
            if (verificationPasswordValid) {
                String verificationPassword = byEmail.get().getVerificationPassword();
                if (verificationPassword.equals(emailVerifyPasswordReq.getEmailCode())) {
                    byEmail.get().setVerificationPassword(null);
                    byEmail.get().setVerificationPasswordExpiry(null);
                    byEmail.get().setEnabled(true);
                    var savedUser = repository.save(byEmail.get());
                    var jwtToken = jwtService.generateToken(savedUser);
                    saveUserToken(savedUser, jwtToken);
                    return new ApiResponse(jwtToken, true);
                } else {
                    return new ApiResponse("Код подтверждения введен неверно", false);
                }
            } else {
                return new ApiResponse("Срок действия кода подтверждения истек", false);
            }
        } else {
            return new ApiResponse("Пользователь с таким email не найден", false);
        }
    }

    public ApiResponse sendEmailAgain(String email) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceAccessException("User not found"));

        String verificationCode = String.valueOf(generateRandom(6));
        setVerificationPasswordWithExpiry(user, verificationCode);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("no-reply@mail.upgradeeducation.uz");
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject("Подтверждение аккаунта");
        simpleMailMessage.setText("Ваш аккаунт: " + email + "\n"
                + "Ваш код подтверждения: " + verificationCode);
        javaMailSender.send(simpleMailMessage);

        return new ApiResponse("Повторная отправка кода верификации на почту", true);
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
        repository.save(user);
    }

    // Метод для проверки, действителен ли verificationPassword в текущий момент
    public boolean isVerificationPasswordValid(User user) {
        if (user.getVerificationPasswordExpiry() == null) {
            return false;
        }
        return new Date().before(user.getVerificationPasswordExpiry());
    }
}
