package com.example.myapptaking.fragment.main.child.sub.chat.model;

import io.rong.message.TextMessage;

import static com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelConfigKt.TEXT;

public class ChatPaneTextEntity extends ChatPanelEntityImp<TextMessage> {

    @Override
    public int getItemType() {
        return TEXT;
    }
}

