package com.alibou.repository;

import com.alibou.entities.Module;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module,Integer> {
    boolean existsByName(String name);
}
