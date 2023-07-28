package com.alibou.payload;

import com.alibou.entities.Lesson;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ModuleReq {
    private Integer moduleNumber;
    private String name;
}
