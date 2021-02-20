package com.example.framework.recycler.multi;

import androidx.annotation.Nullable;

public interface AdapterViewItemViewClick<M extends JssMultiItemEntity> {

    void onAdapterViewListItemViewClick(@Nullable M m, int viewType);

}
