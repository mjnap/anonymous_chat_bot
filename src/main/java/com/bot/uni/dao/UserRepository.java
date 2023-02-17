package com.bot.uni.dao;

import com.bot.uni.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByChatId(String userId);
    Optional<User> findByChatId(String userId);
}
