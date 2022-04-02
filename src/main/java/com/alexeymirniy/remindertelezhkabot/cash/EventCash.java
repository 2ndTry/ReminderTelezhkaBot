package com.alexeymirniy.remindertelezhkabot.cash;

import com.alexeymirniy.remindertelezhkabot.entity.Event;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Getter
@Setter
public class EventCash {

    private final Map<Long, Event> eventMap = new HashMap<>();

    public void saveEventCash(long userId, Event event) {
        eventMap.put(userId, event);
    }
}
