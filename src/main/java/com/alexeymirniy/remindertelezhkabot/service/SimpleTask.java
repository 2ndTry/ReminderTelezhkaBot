package com.alexeymirniy.remindertelezhkabot.service;

import java.util.TimerTask;

public class SimpleTask extends TimerTask {

    private final SendEvent sendEvent;

    public SimpleTask(SendEvent sendEvent) {
        this.sendEvent = sendEvent;
    }

    @Override
    public void run() {
        sendEvent.start();
    }
}
