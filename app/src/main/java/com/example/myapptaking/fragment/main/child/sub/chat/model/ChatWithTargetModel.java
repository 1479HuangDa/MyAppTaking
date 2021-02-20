package com.example.myapptaking.fragment.main.child.sub.chat.model;

import java.io.Serializable;
/**
 * 对方用户信息
 * */
public class ChatWithTargetModel implements Serializable {


    private String targetUserId = "";

    private String targetUserName= "";

    private String targetUserPhoto=  "";

    public ChatWithTargetModel(String targetUserId, String targetUserName, String targetUserPhoto) {
        this.targetUserId = targetUserId;
        this.targetUserName = targetUserName;
        this.targetUserPhoto = targetUserPhoto;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(String targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getTargetUserPhoto() {
        return targetUserPhoto;
    }

    public void setTargetUserPhoto(String targetUserPhoto) {
        this.targetUserPhoto = targetUserPhoto;
    }
}
