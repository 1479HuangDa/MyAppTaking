package com.example.framework.recycler.multi;

import android.content.Context;

public interface ViewHolderItemViewLoader<M> {

    int getItemLayout();

    void convert(Context context, M m);
}
