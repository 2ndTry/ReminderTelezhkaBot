package com.alexeymirniy.remindertelezhkabot.repository;

import com.alexeymirniy.remindertelezhkabot.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    Event findByEventId(Long id);
}
