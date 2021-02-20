package com.example.framework.recycler.multi;


import androidx.annotation.NonNull;

/**
 * 类型池
 *
 * @ M -> 数据类型
 * @ V -> ViewHolder类型
 */
public interface TypePool<M, V> {

    void register(@NonNull Class<? extends M> clazz, @NonNull Class<? extends V> provider);


    Class<? extends V> getProviderByViewType(int viewType);

    Class<? extends M> getModuleByViewType(int viewType);

}