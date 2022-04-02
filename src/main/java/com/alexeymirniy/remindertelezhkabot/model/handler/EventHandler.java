package com.alexeymirniy.remindertelezhkabot.model.handler;

import com.alexeymirniy.remindertelezhkabot.cash.BotStateCash;
import com.alexeymirniy.remindertelezhkabot.cash.EventCash;
import com.alexeymirniy.remindertelezhkabot.dao.EventDao;
import com.alexeymirniy.remindertelezhkabot.dao.UserDao;
import com.alexeymirniy.remindertelezhkabot.entity.Event;
import com.alexeymirniy.remindertelezhkabot.entity.User;
import com.alexeymirniy.remindertelezhkabot.model.BotState;
import com.alexeymirniy.remindertelezhkabot.model.EventFreq;
import com.alexeymirniy.remindertelezhkabot.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.persistence.EntityNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class EventHandler {

    private final BotStateCash botStateCash;
    private final EventCash eventCash;

    private final UserDao userDAO;
    private final EventDao eventDAO;
    private final MenuService menuService;

    @Value("${telegrambot.adminId}")
    private int admin_id;

    @Autowired
    public EventHandler(BotStateCash botStateCash, EventCash eventCash, UserDao userDAO, EventDao eventDAO, MenuService menuService) {
        this.botStateCash = botStateCash;
        this.eventCash = eventCash;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.menuService = menuService;
    }

    public SendMessage saveNewUser(Message message, long userId, SendMessage sendMessage) {
        String userName = message.getFrom().getUserName();
        User user = new User();
        user.setId(userId);
        user.setName(userName);
        user.setOn(true);
        userDAO.saveUser(user);
        sendMessage.setText("В первый сеанс необходимо ввести местное время в формате HH, например, " +
                "если сейчас 21:45, то введите 21, это необходимо для корректнрого оповещения в соответсвии с вашим часовым поясом.");
        botStateCash.saveBotState(userId, BotState.ENTERTIME);

        return sendMessage;
    }

    public BotApiMethod<?> onEvent(Message message) {
        User user = userDAO.findByUserId(message.getFrom().getId());

        boolean on = user.isOn();
        on = !on;
        user.setOn(on);
        userDAO.saveUser(user);
        botStateCash.saveBotState(message.getFrom().getId(), BotState.START);

        return menuService.getMainMenuMessage(message.getChatId(),
                "Изменения сохранены", message.getFrom().getId());
    }

    public BotApiMethod<?> enterLocalTimeUser(Message message) {
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
        Date nowHour = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowHour);
        int num;
        try {
            num = Integer.parseInt(message.getText());
        } catch (NumberFormatException e) {
            sendMessage.setText("Введенные символы не число, посторите ввод");
            return sendMessage;
        }
        if (num < 0 || num > 24) {
            sendMessage.setText("Вы ввели неверное время, повторите.");
            return sendMessage;
        }
        Date userHour;
        try {
            userHour = simpleDateFormat.parse(message.getText());
        } catch (ParseException e) {
            sendMessage.setText("Вы ввели неверное время, повторите.");
            return sendMessage;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(userHour);

        int serverHour = calendar.get(Calendar.HOUR_OF_DAY);
        int clientHour = calendar1.get(Calendar.HOUR_OF_DAY);

        int timeZone = clientHour - serverHour;

        sendMessage.setText("Ваш часовой пояс: " + "+" + timeZone);
        User user = userDAO.findByUserId(userId);
        user.setTimeZone(timeZone);
        userDAO.saveUser(user);
        botStateCash.saveBotState(userId, BotState.START);

        return sendMessage;
    }

    public BotApiMethod<?> removeUserHandler(Message message, long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        User user;
        try {
            long i = Long.parseLong(message.getText());
            user = userDAO.findByUserId(i);
        } catch (NumberFormatException e) {
            sendMessage.setText("Введенная строка не является числом, попробуйте снова!");
            return sendMessage;
        }
        if (user == null) {
            sendMessage.setText("Введенное число отсутсвует в списке, попробуйте снова!");
            return sendMessage;
        }

        userDAO.removeUser(user);
        botStateCash.saveBotState(userId, BotState.START);
        sendMessage.setText("Удаление прошло успешно");

        return sendMessage;
    }

    public BotApiMethod<?> allEvents(long userId) {
        List<Event> list = eventDAO.findAllEvent();
        botStateCash.saveBotState(userId, BotState.START);

        return eventListBuilder(userId, list);
    }

    public BotApiMethod<?> myEventHandler(long userId) {
        List<Event> list = eventDAO.findByUserId(userId);

        return eventListBuilder(userId, list);
    }

    public BotApiMethod<?> eventListBuilder(long userId, List<Event> list) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(String.valueOf(userId));
        StringBuilder builder = new StringBuilder();
        if (list.isEmpty()) {
            replyMessage.setText("Уведомления отсутствуют!");
            return replyMessage;
        }
        for (Event event : list) {
            builder.append(buildEvent(event));
        }
        replyMessage.setText(builder.toString());
        replyMessage.setReplyMarkup(menuService.getInlineMessageButtons());

        return replyMessage;
    }

    public BotApiMethod<?> allUsers(long userId) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(String.valueOf(userId));
        StringBuilder builder = new StringBuilder();
        List<User> list = userDAO.findAllUsers();
        for (User user : list) {
            builder.append(buildUser(user));
        }
        replyMessage.setText(builder.toString());
        replyMessage.setReplyMarkup(menuService.getInlineMessageButtonsAllUser());
        botStateCash.saveBotState(userId, BotState.START);

        return replyMessage;
    }

    private StringBuilder buildUser(User user) {
        StringBuilder builder = new StringBuilder();
        long userId = user.getId();
        String name = user.getName();
        builder.append(userId).append(". ").append(name).append("\n");

        return builder;
    }

    public BotApiMethod<?> editDate(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        long userId = message.getFrom().getId();
        Date date;
        try {
            date = parseDate(message.getText());
        } catch (ParseException e) {
            sendMessage.setText("Не удается распознать указанную дату и время, попробуйте еще раз");
            return sendMessage;
        }
        Event event = eventCash.getEventMap().get(userId);
        event.setDate(date);
        eventCash.saveEventCash(userId, event);

        return editEvent(message.getChatId(), userId);
    }

    public BotApiMethod<?> editDescription(Message message) {
        String description = message.getText();
        long userId = message.getFrom().getId();
        if (description.isEmpty() || description.length() < 4 || description.length() > 200) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Описание должно быть минимум 4 символа, но не более 200");
            return sendMessage;
        }
        Event event = eventCash.getEventMap().get(userId);
        event.setDescription(description);
        eventCash.saveEventCash(userId, event);

        return editEvent(message.getChatId(), userId);
    }

    public BotApiMethod<?> editHandler(Message message, long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        Event eventRes;
        try {
            eventRes = enterNumberEvent(message.getText(), userId);
        } catch (NumberFormatException e) {
            sendMessage.setText("Введенная строка не является числом, попробуйте снова!");
            return sendMessage;
        }
        if (eventRes == null) {
            sendMessage.setText("Введенное число отсутсвует в списке, попробуйте снова!");
            return sendMessage;
        }

        eventCash.saveEventCash(userId, eventRes);
        StringBuilder builder = buildEvent(eventRes);
        sendMessage.setText(builder.toString());
        sendMessage.setReplyMarkup(menuService.getInlineMessageForEdit());

        return sendMessage;
    }

    public BotApiMethod<?> enterDateHandler(Message message, long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        Date date;
        try {
            date = parseDate(message.getText());
        } catch (ParseException e) {
            sendMessage.setText("Не удается распознать указанную дату и время, попробуйте еще раз");
            return sendMessage;
        }

        Event event = eventCash.getEventMap().get(userId);
        event.setDate(date);
        eventCash.saveEventCash(userId, event);
        sendMessage.setText("Выберите период повторения(Единоразово(сработает один раз и удалится), " +
                "Ежедневно в указанный час, " +
                "1 раз в месяц в указанную дату, 1 раз в год в указанное число)");
        sendMessage.setReplyMarkup(menuService.getInlineMessageButtonsForEnterDate());

        return sendMessage;
    }

    public BotApiMethod<?> enterDescriptionHandler(Message message, long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        String description = message.getText();
        if (description.isEmpty() || description.length() < 4 || description.length() > 200) {
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Описание должно быть минимум 4 символа, но не более 200");
            return sendMessage;
        }
        botStateCash.saveBotState(userId, BotState.ENTERDATE);
        Event event = eventCash.getEventMap().get(userId);
        event.setDescription(description);
        eventCash.saveEventCash(userId, event);
        sendMessage.setText("Введите дату предстоящего события в формате DD.MM.YYYY HH:MM, например - 02.06.2021 21:24, либо 02.06.2021");

        return sendMessage;
    }

    private Event enterNumberEvent(String message, long userId) throws NumberFormatException, NullPointerException, EntityNotFoundException {
        List<Event> list;
        if (userId == admin_id) {
            list = eventDAO.findAllEvent();
        } else {
            list = eventDAO.findByUserId(userId);
        }

        int i = Integer.parseInt(message);

        return list.stream().filter(event -> event.getEventId() == i).findFirst().orElseThrow(null);
    }

    public BotApiMethod<?> removeEventHandler(Message message, long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        Event eventRes;
        try {
            eventRes = enterNumberEvent(message.getText(), userId);
        } catch (NumberFormatException e) {
            sendMessage.setText("Введенная строка не является числом, попробуйте снова!");
            return sendMessage;
        }
        if (eventRes == null) {
            sendMessage.setText("Введенное число отсутсвует в списке, попробуйте снова!");
            return sendMessage;
        }

        eventDAO.removeEvent(eventRes);
        botStateCash.saveBotState(userId, BotState.START);
        sendMessage.setText("Удаление прошло успешно");
        return sendMessage;
    }

    private StringBuilder buildEvent(Event event) {
        StringBuilder builder = new StringBuilder();
        long eventId = event.getEventId();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date = event.getDate();
        String dateFormat = simpleDateFormat.format(date);

        String description = event.getDescription();
        EventFreq freq = event.getFreq();
        String freqEvent;
        switch (freq.name()) {
            case ("TIME"):
                freqEvent = "Единоразово";
                break;
            case ("EVERYDAY"):
                freqEvent = "Ежедневно";
                break;
            case ("MONTH"):
                freqEvent = "Один раз в месяц";
                break;
            case ("YEAR"):
                freqEvent = "Один раз в год";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + freq.name());
        }
        builder.append(eventId).append(". ").append(dateFormat).append(": ")
                .append(description).append(": ").append(freqEvent).append("\n");

        return builder;
    }

    public SendMessage editEvent(long chatId, long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        Event event = eventCash.getEventMap().get(userId);
        if (event.getEventId() == 0) {
            sendMessage.setText("Не удалось сохранить пользователя, нарушена последовательность действий");
            return sendMessage;
        }
        eventDAO.saveEvent(event);
        sendMessage.setText("Изменение сохранено");
        eventCash.saveEventCash(userId, new Event());

        return sendMessage;
    }

    public SendMessage saveEvent(EventFreq freq, long userId, long chatId) {
        Event event = eventCash.getEventMap().get(userId);
        event.setFreq(freq);
        event.setUser(userDAO.findByUserId(userId));
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        eventDAO.saveEvent(event);
        eventCash.saveEventCash(userId, new Event());
        sendMessage.setText("Напоминание успешно сохранено");
        botStateCash.saveBotState(userId, BotState.START);

        return sendMessage;
    }

    private Date parseDate(String s) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        return simpleDateFormat.parse(s);
    }
}
