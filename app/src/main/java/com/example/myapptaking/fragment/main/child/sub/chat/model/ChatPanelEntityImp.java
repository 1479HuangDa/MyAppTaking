package com.example.myapptaking.fragment.main.child.sub.chat.model;

import com.example.framework.bmob.IMUser;

import org.jetbrains.annotations.NotNull;

import io.rong.imlib.model.MessageContent;

public abstract class ChatPanelEntityImp<T extends MessageContent> implements ChatPanelEntity {

    private String senderUserId = "";

    private ChatWithTargetModel mTarget;

    private IMUser iMUser;

    private T msg;

    public T getMsg() {
        return msg;
    }

    public void setMsg(T msg) {
        this.msg = msg;
    }

    public ChatWithTargetModel getTarget() {
        return mTarget;
    }

    public void setTarget(ChatWithTargetModel mTarget) {
        this.mTarget = mTarget;

    }

    @Override
    public boolean isReceived() {
        if (senderUserId != null && mTarget != null) {
            return !senderUserId.equals(iMUser.getObjectId());
        }
        return false;
    }

    @Override
    public void setSenderUserId( String senderUserId) {
        this.senderUserId = senderUserId;
    }

    @NotNull
    @Override
    public String getSenderUserId() {
        return senderUserId;
    }

    @Override
    public void setIMUser(IMUser iMUser) {
        this.iMUser = iMUser;
    }

    @NotNull
    @Override
    public IMUser getIMUser() {
        return iMUser;
    }
}
