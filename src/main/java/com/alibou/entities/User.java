package com.alibou.entities;

import com.alibou.entities.Enums.Role;
import com.alibou.token.Token;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue
    private Integer id;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;
    private String password;
    @Temporal(TemporalType.DATE) // Указываем, что хотим сохранять только дату без времени
    private Date dateOfBirth;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;

    @CreationTimestamp // Автоматически устанавливает дату создания при сохранении
    @Temporal(TemporalType.DATE) // Указываем, что хотим сохранять только дату без времени
    @Column(name = "registration_date")
    private Date registrationDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Token> tokens;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    private Date lastModified;
    private boolean enabled = false;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "lesson_views",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "lesson_id")}
    )
    private Set<Lesson> viewedLessons = new HashSet<>();
    private String verificationPassword;
    private Date verificationPasswordExpiry;

    public void setVerificationPasswordWithExpiry(String verificationPassword, int expiryMinutes) {
        this.verificationPassword = verificationPassword;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, expiryMinutes);
        this.verificationPasswordExpiry = calendar.getTime();
    }

    public boolean isVerificationPasswordValid() {
        if (verificationPasswordExpiry == null) {
            return false;
        }
        return new Date().before(verificationPasswordExpiry);
    }


    public Lesson deleteLesson(Lesson lesson) {
        viewedLessons.remove(lesson);
        lesson.getUsers().remove(this);
        return lesson;
    }

    public Lesson viewLesson(Lesson lesson) {
        viewedLessons.add(lesson);
        lesson.getUsers().add(this);
        return lesson;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
