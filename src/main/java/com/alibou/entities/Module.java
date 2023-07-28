package com.alibou.entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Module {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer modulNumber;

    private String name;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "attachment_id")
    private Attachment attachment;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "module")
    private List<Lesson> lessons;

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }
}