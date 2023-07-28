package com.alibou.repository;

import com.alibou.entities.Lesson;
import com.alibou.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    boolean existsByName(String name);
    Optional<Lesson> findById(Long id);
    List<Lesson> findByModuleId(Integer id);


}
