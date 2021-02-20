package com.example.myapptaking.fragment.main.child.sub.chat.model;

import io.rong.message.LocationMessage;

import static com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelConfigKt.LOCATION;

public class ChatPaneLocationEntity extends ChatPanelEntityImp<LocationMessage> {


    @Override
    public int getItemType() {
        return LOCATION;
    }
}
