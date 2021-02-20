package com.example.framework.recycler.multi;

import android.content.Context;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.framework.recycler.JssBaseViewHolder;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class JssMultiItemViewHolder<M> extends JssBaseViewHolder
        implements ViewHolderItemViewLoader<M>,
        OnItemChildClickListener,
        OnItemChildLongClickListener ,ViewHolderStateListener{

    private final HashSet<Integer> nestViews;

    private final LinkedHashSet<Integer> childClickViewIds;

    private final LinkedHashSet<Integer> itemChildLongClickViewIds;

    protected BaseQuickAdapter adapter;

    public JssMultiItemViewHolder(View view) {
        super(view);
        this.childClickViewIds = new LinkedHashSet<>();
        this.itemChildLongClickViewIds = new LinkedHashSet<>();
        this.nestViews = new HashSet<>();
    }


//    @Override
    protected BaseViewHolder setAdapter(BaseQuickAdapter adapter) {
//        super.setAdapter(adapter);
        this.adapter = adapter;
        return this;
    }

//    @Override/
    public BaseViewHolder setNestView(@IdRes int... viewIds) {
//        super.setAdapter(adapter);
        for (int viewId : viewIds) {
            nestViews.add(viewId);
        }
        addOnClickListener(viewIds);
        addOnLongClickListener(viewIds);
        return this;
    }


    @Override
    public <T extends View> T findView(int $this$findView) {
        return super.findView($this$findView);
    }
    public <T extends View> T findView22(int $this$findView) {
        return super.findView($this$findView);
    }

    //    @Override
    public BaseViewHolder addOnLongClickListener(int... viewIds) {
//        super.addOnLongClickListener(viewIds);

        for (int viewId : viewIds) {
            itemChildLongClickViewIds.add(viewId);
            final View view = getView(viewId);
            if (view != null) {
                if (!view.isLongClickable()) {
                    view.setLongClickable(true);
                }
                view.setOnLongClickListener(v -> {

                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return false;
                    }
                    position -= adapter.getHeaderLayoutCount();
                    return onItemChildLongClick(adapter, v, position);
                });
            }
        }
        return this;
    }

//    @Override
    public BaseViewHolder addOnClickListener(@IdRes final int... viewIds) {
//        super.addOnClickListener(viewIds);

        for (int viewId : viewIds) {
            childClickViewIds.add(viewId);
            final View view = getView(viewId);
            if (view != null) {
                if (!view.isClickable()) {
                    view.setClickable(true);
                }
                view.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return;
                    }
                    position -= adapter.getHeaderLayoutCount();
                    onItemChildClick(adapter, v, position);
                });
            }
        }
        return this;
    }

    /**
     *
     * 为了可以在Fragment/Activity中响应相应事件
     * （如页面跳转 其他UI刷新。。。）
     *
     * 在它子类也会响应相应事件，只需重写该方法（只是无法完成Fragment之间跳转，Activity之间跳转可完成）
     * */
    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {

       OnItemChildClickListener clickListener = adapter.getOnItemChildClickListener();

        if (clickListener != null) {
            clickListener.onItemChildClick(adapter, view, position);
        }
    }

    /**
     *
     * 为了可以在Fragment/Activity中响应相应事件
     * （如页面跳转 其他UI刷新。。。）
     *
     * 在它子类也会响应相应事件，只需重写该方法（只是无法完成Fragment之间跳转，Activity之间跳转可完成）
     * */
    @Override
    public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position) {

        OnItemChildLongClickListener clickListener = adapter.getOnItemChildLongClickListener();

        if (clickListener != null) {
            return clickListener.onItemChildLongClick(adapter, view, position);
        }
        return false;
    }

    @Override
    public int getItemLayout() {
        return 0;
    }

    @Override
    public void convert(Context context, M m) {

    }

    @Override
    public void onViewAttachedToWindow() {

    }

    @Override
    public void onViewDetachedFromWindow() {

    }
}
