package com.alexeymirniy.remindertelezhkabot.service;

import com.alexeymirniy.remindertelezhkabot.dao.EventCashDao;
import com.alexeymirniy.remindertelezhkabot.dao.EventDao;
import com.alexeymirniy.remindertelezhkabot.entity.Event;
import com.alexeymirniy.remindertelezhkabot.entity.EventCashEntity;
import com.alexeymirniy.remindertelezhkabot.model.EventFreq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

@EnableScheduling
@Service
public class EventService {

    private final EventDao eventDAO;
    private final EventCashDao eventCashDAO;

    @Autowired
    public EventService(EventDao eventDAO, EventCashDao eventCashDAO) {
        this.eventDAO = eventDAO;
        this.eventCashDAO = eventCashDAO;
    }

    @Scheduled(fixedRateString = "${eventservice.period}")
    private void eventService() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        List<Event> list = eventDAO.findAllEvent().stream().filter(event -> {
            if (event.getUser().isOn()) {
                EventFreq eventFreq = event.getFreq();

                Calendar calendarUserTime = getDateUserTimeZone(event);

                int day1 = calendarUserTime.get(Calendar.DAY_OF_MONTH);
                int month1 = calendarUserTime.get(Calendar.MONTH);
                int year1 = calendarUserTime.get(Calendar.YEAR);
                switch (eventFreq.name()) {
                    case "TIME":
                        if (day == day1 && month == month1 && year == year1) {
                            eventDAO.removeEvent(event);
                            return true;
                        }else
                            return false;
                    case "EVERYDAY":
                        return true;
                    case "MONTH":
                        return day == day1;
                    case "YEAR":
                        return day == day1 && month == month1;
                    default: return false;
                }
            } else return false;
        }).collect(Collectors.toList());

        for (Event event : list) {
            Calendar calendarUserTime = getDateUserTimeZone(event);
            int hour1 = calendarUserTime.get(Calendar.HOUR_OF_DAY);
            calendarUserTime.set(year, month, day, hour1, 0, 0);

            String description = event.getDescription();
            String userId = String.valueOf(event.getUser().getId());

            //save the event to the database in case the server reboots.
            EventCashEntity eventCashEntity = EventCashEntity.eventTo(calendarUserTime.getTime(), event.getDescription(), event.getUser().getId());
            eventCashDAO.saveEventCashEntity(eventCashEntity);

            //create a thread for the upcoming event with the launch at a specific time
            SendEvent sendEvent = new SendEvent();
            sendEvent.setSendMessage(new SendMessage(userId, description));
            sendEvent.setEventCashId(eventCashEntity.getId());

            new Timer().schedule(new SimpleTask(sendEvent), calendarUserTime.getTime());
        }
    }

    private Calendar getDateUserTimeZone(Event event) {
        Calendar calendarUserTime = Calendar.getInstance();
        calendarUserTime.setTime(event.getDate());
        int timeZone = event.getUser().getTimeZone();

        calendarUserTime.add(Calendar.HOUR_OF_DAY, -timeZone);
        return calendarUserTime;
    }
}