package com.alibou.payload;

import com.alibou.entities.Lesson;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ModuleRes {
    private Integer id;
    private Integer moduleNumber;
    private String name;
    private UUID icon;
    private List<LessonRes> lessons;
    public void addLessonId(LessonRes lesson){
        lessons.add(lesson);
    }

}
