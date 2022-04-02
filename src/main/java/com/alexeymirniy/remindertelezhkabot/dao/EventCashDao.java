package com.alexeymirniy.remindertelezhkabot.dao;

import com.alexeymirniy.remindertelezhkabot.entity.EventCashEntity;
import com.alexeymirniy.remindertelezhkabot.repository.EventCashRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventCashDao {

    private EventCashRepository eventCashRepository;

    @Autowired
    public void setEventCashRepository(EventCashRepository eventCashRepository) {
        this.eventCashRepository = eventCashRepository;
    }

    public List<EventCashEntity> findAllEventCash() {
        return eventCashRepository.findAll();
    }

    public void deleteEventCashEntityById(long id) {
        eventCashRepository.deleteById(id);
    }

    public void saveEventCashEntity(EventCashEntity eventCashEntity) {
        eventCashRepository.save(eventCashEntity);
    }
}
