package com.alexeymirniy.remindertelezhkabot.repository;

import com.alexeymirniy.remindertelezhkabot.entity.EventCashEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventCashRepository extends JpaRepository<EventCashEntity, Long> {
    EventCashEntity findById(long id);
}
