package com.alexeymirniy.remindertelezhkabot.service;

import com.alexeymirniy.remindertelezhkabot.config.ApplicationContextProvider;
import com.alexeymirniy.remindertelezhkabot.dao.EventCashDao;
import com.alexeymirniy.remindertelezhkabot.model.TelegramBot;
import lombok.Setter;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Setter
public class SendEvent extends Thread{

    private long eventCashId;
    private SendMessage sendMessage;

    public SendEvent() {
    }

    @SneakyThrows
    @Override
    public void run() {
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        EventCashDao eventCashDao = ApplicationContextProvider.getApplicationContext().getBean(EventCashDao.class);
        telegramBot.execute(sendMessage);

        eventCashDao.deleteEventCashEntityById(eventCashId);
    }
}
