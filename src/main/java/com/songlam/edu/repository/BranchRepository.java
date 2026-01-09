package com.songlam.edu.repository;

import com.songlam.edu.entity.Branches;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branches, Long>  {

    boolean existsByName(String name);

}
