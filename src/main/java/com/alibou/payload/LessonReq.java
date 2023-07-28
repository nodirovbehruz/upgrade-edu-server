package com.alibou.payload;

import com.alibou.entities.Attachment;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LessonReq {
    private String name;
    private Integer lessonNumber;
    private Integer moduleId;
    private String description;

    private String homework;

}
