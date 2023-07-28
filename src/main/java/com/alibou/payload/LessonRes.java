package com.alibou.payload;

import com.alibou.entities.Attachment;
import com.alibou.entities.Module;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class LessonRes {
    private Long id;
    private Integer lessonNumber;
    private UUID videoId;
    private String name;
    private Integer moduleId;
    private String description;
    private UUID bonus1;
    private UUID bonus2;
    private UUID bonus3;

    private String homework;

}
