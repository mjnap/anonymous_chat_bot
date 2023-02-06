package com.bot.uni.dao;

import com.bot.uni.model.FieldOfStudy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FieldOfStudyRepository extends JpaRepository<FieldOfStudy, Long> {
    List<FieldOfStudy> findAllByUni(String uni);
}
