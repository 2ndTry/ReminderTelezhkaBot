package com.alexeymirniy.remindertelezhkabot.repository;

import com.alexeymirniy.remindertelezhkabot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findById(long id);
}
