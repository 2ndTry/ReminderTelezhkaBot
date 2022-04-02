package com.alexeymirniy.remindertelezhkabot.service;

import com.alexeymirniy.remindertelezhkabot.dao.EventCashDao;
import com.alexeymirniy.remindertelezhkabot.entity.EventCashEntity;
import com.alexeymirniy.remindertelezhkabot.model.TelegramBot;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

@Component
public class SendEventFromCash {

    private final EventCashDao eventCashDao;
    private final TelegramBot telegramBot;

    @Value("${telegrambot.adminId}")
    private int admin_id;

    @Autowired
    public SendEventFromCash(EventCashDao eventCashDao, TelegramBot telegramBot) {
        this.eventCashDao = eventCashDao;
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    @SneakyThrows
    private void afterStart() {
        List<EventCashEntity> list = eventCashDao.findAllEventCash();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(admin_id));
        sendMessage.setText("Произошла перезагрузка!");

        telegramBot.execute(sendMessage);

        if (!list.isEmpty()) {
            for (EventCashEntity eventCashEntity : list) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(eventCashEntity.getDate());
                SendEvent sendEvent = new SendEvent();
                sendEvent.setSendMessage(new SendMessage(String.valueOf(eventCashEntity.getUserId()),
                        eventCashEntity.getDescription()));
                sendEvent.setEventCashId(eventCashEntity.getId());
                new Timer().schedule(new SimpleTask(sendEvent), calendar.getTime());
            }
        }
    }
}
