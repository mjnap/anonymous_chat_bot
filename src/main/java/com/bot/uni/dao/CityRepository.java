package com.bot.uni.dao;

import com.bot.uni.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findAllByState_Id(Long stateId);
}
