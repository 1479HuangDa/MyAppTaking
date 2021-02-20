package com.example.framework.recycler;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.GridSpanSizeLookup;
import com.chad.library.adapter.base.module.LoadMoreModule;

import java.util.List;

public class JssBaseQuickAdapter<T> extends BaseQuickAdapter<T, JssBaseViewHolder>
        implements GridSpanSizeLookup , LoadMoreModule {

    public JssBaseQuickAdapter(int layoutResId) {
        super(layoutResId);
        setGridSpanSizeLookup(this);
//        setSpanSizeLookup(this);
    }

    public JssBaseQuickAdapter(int layoutResId, List<T> mData) {
        super(layoutResId, mData);
        setGridSpanSizeLookup(this);
//        setSpanSizeLookup(this);

    }


    public RecyclerView getJssRecyclerView() {
        return super.getRecyclerView();
    }

    @Override
    protected void convert(JssBaseViewHolder helper, T item) {

    }

    public void clears() {
        getData().clear();
        notifyDataSetChanged();
    }

//    @Override
//    public int getSpanSize(GridLayoutManager manager, int position) {
//
////        T mItem =  getItem(position);
////        if (mItem == null) return manager.getSpanCount();
//
//        Log.d("getSpanSize", "getSpanSize: "+position);
//
//        int type = getItemViewType(position);
//
//        if (type == BaseQuickAdapter.EMPTY_VIEW
//                || type == BaseQuickAdapter.LOADING_VIEW
//                || type == BaseQuickAdapter.HEADER_VIEW
//                || type == BaseQuickAdapter.FOOTER_VIEW) {
//
//            return manager.getSpanCount();
//        } else {
//            return   1;
//
//        }
//    }


    @Override
    public int getSpanSize(@NonNull GridLayoutManager manager, int viewType, int position) {
        int spanSize = 1;

        if (viewType == BaseQuickAdapter.EMPTY_VIEW
                || viewType == BaseQuickAdapter.LOAD_MORE_VIEW
                || viewType == BaseQuickAdapter.HEADER_VIEW
                || viewType == BaseQuickAdapter.FOOTER_VIEW) {
            spanSize = manager.getSpanCount();

        }
        return spanSize;
    }
}