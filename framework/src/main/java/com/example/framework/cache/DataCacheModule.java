package com.example.framework.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DataCacheModule implements Serializable {

    private List<Serializable> beanList = new ArrayList<>();

    public List<Serializable> getBeanList() {
        return beanList;
    }

    public void setBeanList(List<Serializable> beanList) {
        this.beanList = beanList;
    }
}