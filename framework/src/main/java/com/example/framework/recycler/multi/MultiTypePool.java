package com.example.framework.recycler.multi;

import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;

/**
 * 类型池
 */
public class MultiTypePool<M extends JssMultiItemEntity, V> implements TypePool<M, V> {

    private final ArrayMap<Class<? extends M>, Class<? extends V>> mProviders = new ArrayMap<>();

    private final SparseArray<Class<? extends M>> mContents = new SparseArray<>();


    /**
     * 将数据类型和 ViewHolder 类型关联起来
     *
     * @param clazz    数据类型
     * @param provider ViewHolder类型
     */
    @Override
    public void register(@NonNull Class<? extends M> clazz, @NonNull Class<? extends V> provider) {

        try {
            M m = clazz.newInstance();

            int itemType = m.getItemType();

            if (mContents.get(itemType) != null) {
                Log.w("register", "mContents have already the" + clazz.getSimpleName() + "`s" + itemType);
            }

            if (mProviders.get(clazz) != null) {
                Log.w("register", "mProviders have already the" + clazz.getSimpleName() + "`s" + provider.getSimpleName());
            }
            mProviders.put(clazz, provider);

            mContents.put(itemType, clazz);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<? extends V> getProviderByViewType(int viewType) {
        return mProviders.get(mContents.get(viewType));
    }

    @Override
    public Class<? extends M> getModuleByViewType(int viewType) {
        return mContents.get(viewType);
    }

    ArrayMap<Class<? extends M>, Class<? extends V>> getProviders() {
        return mProviders;
    }
}