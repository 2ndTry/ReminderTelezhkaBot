package com.alexeymirniy.remindertelezhkabot.dao;

import com.alexeymirniy.remindertelezhkabot.entity.Event;
import com.alexeymirniy.remindertelezhkabot.entity.User;
import com.alexeymirniy.remindertelezhkabot.repository.EventRepository;
import com.alexeymirniy.remindertelezhkabot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventDao {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public EventDao(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public List<Event> findByUserId(long userId) {
        User user = userRepository.findById(userId);
        return user.getEvents();
    }

    public List<Event> findAllEvent() {
        return eventRepository.findAll();
    }

    public Event findByEvent(long eventId) {
        return eventRepository.findByEventId(eventId);
    }

    public void removeEvent(Event event) {
        eventRepository.delete(event);
    }

    public void saveEvent(Event event) {
        eventRepository.save(event);
    }
}
