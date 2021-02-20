package com.example.myapptaking.fragment.main.child.model;

import com.example.framework.bmob.IMUser;
import com.example.framework.bmob.SquareSet;
import com.example.framework.recycler.multi22.JssNewMultiItemEntity;

public class SquareModel implements JssNewMultiItemEntity {

    private SquareSet mSquareSet;

    private IMUser imUser;

    public SquareModel(SquareSet mSquareSet, IMUser imUser) {
        this.mSquareSet = mSquareSet;
        this.imUser = imUser;
    }

    public SquareSet getSquareSet() {
        return mSquareSet;
    }

    public void setSquareSet(SquareSet mSquareSet) {
        this.mSquareSet = mSquareSet;
    }

    public IMUser getImUser() {
        return imUser;
    }

    public void setImUser(IMUser imUser) {
        this.imUser = imUser;
    }

    @Override
    public int getItemType() {
        return mSquareSet.getPushType();
    }
}
