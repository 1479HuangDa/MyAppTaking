package com.example.myapptaking.fragment.main.child.model;

import com.example.myapptaking.R;

/**
 * FileName: StarModel
 * Founder: LiuGuiLin
 * Profile: 星球View的数据模型
 */
public class StarModel {

    //昵称
    private String nickName;
    //ID
    private String userId;
    //头像
    private String photoUrl;

    private int icon= R.drawable.img_star_icon_3;

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
