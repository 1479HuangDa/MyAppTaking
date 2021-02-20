package com.example.framework.cache;

import androidx.annotation.NonNull;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class JssDataCache extends LitePalSupport implements Serializable {
    //PrimaryKey 必须要有,且不为空,autoGenerate 主键的值是否由Room自动生成,默认false

    @NonNull
    @Column(unique = true)
    public String key;

    //指定该字段在表中的列的名字

    public byte[] data;

}