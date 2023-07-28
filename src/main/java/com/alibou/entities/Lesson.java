package com.alibou.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer lessonNumber;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Module module;
    private String description;
    private String homework;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attachment_id")
    private Attachment video;
    @ManyToMany(mappedBy = "viewedLessons")
    private Set<User> users = new HashSet<>();
    @ManyToOne(cascade = CascadeType.REMOVE)
    private Attachment bonus1;
    @ManyToOne(cascade = CascadeType.REMOVE)
    private Attachment bonus2;
    @ManyToOne(cascade = CascadeType.REMOVE)
    private Attachment bonus3;


}
