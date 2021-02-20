package com.example.framework.adapter;

import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;
import java.util.List;

import kotlin.jvm.JvmOverloads;

/**
 * FileName: CommonAdapter
 * Founder: LiuGuiLin
 * Profile: 万能的适配器
 */
public class CommonAdapter<T> extends RecyclerView.Adapter<CommonViewHolder> {

    private List<T> mList;

    private OnBindDataListener<T> onBindDataListener;
    private OnMoreBindDataListener<T> onMoreBindDataListener;

    public CommonAdapter(List<T> mList, OnBindDataListener<T> onBindDataListener) {
        this.mList = mList;
        this.onBindDataListener = onBindDataListener;
    }

    public CommonAdapter(List<T> mList, OnMoreBindDataListener<T> onMoreBindDataListener) {
        this.mList = mList;
        this.onBindDataListener = onMoreBindDataListener;
        this.onMoreBindDataListener = onMoreBindDataListener;
    }


    @Override
    public int getItemViewType(int position) {
        if (onMoreBindDataListener != null) {
            return onMoreBindDataListener.getItemType(position);
        }
        return 0;
    }

    @Override
    public CommonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = onBindDataListener.getLayoutId(viewType);
        CommonViewHolder viewHolder = CommonViewHolder.getViewHolder(parent, layoutId);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommonViewHolder holder, int position) {

        onBindDataListener.onBindViewHolder(
                mList.get(position), holder, getItemViewType(position), position);
    }

    public void clear(){
        if (mList!=null){
            mList.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }
}