package com.alexeymirniy.remindertelezhkabot.model.handler;

import com.alexeymirniy.remindertelezhkabot.cash.BotStateCash;
import com.alexeymirniy.remindertelezhkabot.cash.EventCash;
import com.alexeymirniy.remindertelezhkabot.dao.UserDao;
import com.alexeymirniy.remindertelezhkabot.entity.Event;
import com.alexeymirniy.remindertelezhkabot.model.BotState;
import com.alexeymirniy.remindertelezhkabot.service.MenuService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
public class MessageHandler {

    private final UserDao userDao;
    private final MenuService menuService;
    private final com.alexeymirniy.remindertelezhkabot.model.handler.EventHandler eventHandler;
    private final BotStateCash botStateCash;
    private final EventCash eventCash;

    public MessageHandler(UserDao userDao,
                          MenuService menuService,
                          EventHandler eventHandler,
                          BotStateCash botStateCash,
                          EventCash eventCash) {
        this.userDao = userDao;
        this.menuService = menuService;
        this.eventHandler = eventHandler;
        this.botStateCash = botStateCash;
        this.eventCash = eventCash;
    }

    public BotApiMethod<?> handle(Message message, BotState botState) {

        long userId = message.getFrom().getId();
        long chatId = message.getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        if (!userDao.isExist(userId)) {
            return eventHandler.saveNewUser(message, userId, sendMessage);
        }

        botStateCash.saveBotState(userId, botState);

        switch (botState.name()) {
            case ("START"):
                return menuService.getMainMenuMessage(message.getChatId(),
                        "Воспользуйтесь главным меню", userId);
            case ("ENTERTIME"):
                return eventHandler.enterLocalTimeUser(message);
            case ("MYEVENTS"):
                return eventHandler.myEventHandler(userId);
            case ("ENTERNUMBEREVENT"):
                return eventHandler.removeEventHandler(message, userId);
            case ("ENTERDESCRIPTION"):
                return eventHandler.enterDescriptionHandler(message, userId);
            case ("ENTERDATE"):
                return eventHandler.enterDateHandler(message, userId);
            case ("CREATE"):
                botStateCash.saveBotState(userId, BotState.ENTERDESCRIPTION);
                eventCash.saveEventCash(userId, new Event());
                sendMessage.setText("Введите описание события");
                return sendMessage;
            case ("ENTERNUMBERFOREDIT"):
                return eventHandler.editHandler(message, userId);
            case ("EDITDESCRIPTION"):
                return eventHandler.editDescription(message);
            case ("EDITDATE"):
                return eventHandler.editDate(message);
            case ("ALLEVENTS"):
                return eventHandler.allEvents(userId);
            case ("ALLUSERS"):
                return eventHandler.allUsers(userId);
            case ("ONEVENT"):
                return eventHandler.onEvent(message);
            case ("ENTERNUMBERUSER"):
                return eventHandler.removeUserHandler(message, userId);
            default:
                throw new IllegalStateException("Unexpected value: " + botState);
        }
    }
}
