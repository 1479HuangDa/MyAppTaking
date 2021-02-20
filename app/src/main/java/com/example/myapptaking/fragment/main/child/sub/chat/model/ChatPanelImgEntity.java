package com.example.myapptaking.fragment.main.child.sub.chat.model;

import io.rong.message.ImageMessage;

import static com.example.myapptaking.fragment.main.child.sub.chat.model.ChatPanelConfigKt.IMAGE;

public class ChatPanelImgEntity extends ChatPanelEntityImp<ImageMessage> {



    @Override
    public int getItemType() {
        return IMAGE;
    }
}
