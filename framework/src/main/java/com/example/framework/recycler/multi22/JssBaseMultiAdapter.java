package com.example.framework.recycler.multi22;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseDelegateMultiAdapter;
import com.chad.library.adapter.base.BaseProviderMultiAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.chad.library.adapter.base.module.BaseUpFetchModule;
import com.example.framework.base.BaseUIFragment;
import com.example.framework.recycler.JssBaseViewHolder;
import com.example.framework.recycler.multi.JssMultiItemEntity;
import com.example.framework.recycler.multi22.Basic.JssBaseProviderMultiAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class JssBaseMultiAdapter<T extends JssNewMultiItemEntity>
        extends JssBaseProviderMultiAdapter<T, JssBaseViewHolder> {

    public JssBaseMultiAdapter() {
        super();
        addListItemProvider(this);
    }



    public JssBaseMultiAdapter(@Nullable List<T> data) {
        super(data);
        addListItemProvider(this);
    }

    @Override
    protected int getItemType(@NotNull List<? extends T> list, int position) {
        return list.get(position).getItemType();
    }

    public void clears() {
        getData().clear();
        notifyDataSetChanged();
    }



    public RecyclerView getJssRecyclerView() {
        return super.getRecyclerView();

    }

    protected abstract void  addListItemProvider(JssBaseMultiAdapter<T> jssBaseMultiAdapter);
}
