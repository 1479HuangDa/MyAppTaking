package com.example.framework.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.listener.OnLoadMoreListener;
import com.chad.library.adapter.base.module.BaseLoadMoreModule;
import com.example.framework.R;
import com.example.framework.cache.DataCacheModule;
import com.example.framework.cache.JssCacheManager;
import com.example.framework.global.AppGlobals;
import com.example.framework.model.CommonBean;
import com.example.framework.recycler.JssBaseQuickAdapter;
import com.example.framework.recycler.JssBaseViewHolder;
import com.example.framework.utils.LogUtils;
import com.example.framework.utils.NetworkUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class SimpleListFragment<T extends Serializable> extends LazyFragment implements OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, OnLoadMoreListener {

    private String Save_TAG = "";

    protected LinearLayout mLayoutRoot;

    protected LinearLayout mHeader;

    protected LinearLayout mFooter;

    protected RecyclerView mRecyclerView;

    protected JssBaseQuickAdapter<T> mAdapter;

    protected boolean isAlwaysRefresh = true;

    protected boolean isEnableLoadMore = true;

    protected boolean isAddDividerItemDecoration = true;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected List<T> mData = new ArrayList<>();

//    protected Gson gson = new GsonBuilder().create();

    protected final int mPrimaryPageIndex = 1;

    protected int pageIndex = mPrimaryPageIndex;

    protected ViewGroup list_empty_view;

    protected int delayedTime = 1000;

    protected FrameLayout content_frame;

    protected boolean isInitialRefresh = true;

    protected Gson gson = new GsonBuilder().create();

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        if (outState == null && mAdapter == null) return;

        CommonBean<List<T>> bean = new CommonBean<>();
//
        bean.setData(mAdapter.getData());

        if (TextUtils.isEmpty(Save_TAG)) {

            Save_TAG = mAdapter.getData().getClass().getSimpleName() +
                    "-" +
                    bean.hashCode() +
                    "-"
//                    + JssUserManager.getUserToken().getUserId().hashCode()
            ;
        }

        DataCacheModule dataCacheModule = new DataCacheModule();

        List<T> data = bean.getData();

        List<Serializable> beanList = dataCacheModule.getBeanList();

        for (int i = 0; i < data.size(); i++) {

            beanList.add(data.get(i));
        }
        if (outState != null) {
            outState.putString("Save_TAG", Save_TAG);
        }

        JssCacheManager.save(Save_TAG, dataCacheModule);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null && mAdapter != null) {

            Save_TAG = savedInstanceState.getString("Save_TAG");

            JssCacheManager.getCache(Save_TAG, o -> {
                LogUtils.i("data: " + o.toString());

                if (o instanceof DataCacheModule) {
//                    String data = (String) o;
                    DataCacheModule dataCacheModule = (DataCacheModule) o;
                    mAdapter.clears();
                    List<Serializable> beanList = dataCacheModule.getBeanList();
                    for (int i = 0; i < beanList.size(); i++) {
                        mData.add((T) beanList.get(i));
//                        mAdapter.addData((T) beanList.get(i));
                    }
                    if (mData.size() > 0) {
                        setIsLoad(true);
                        mAdapter.notifyDataSetChanged();
                    }
                }
                LogUtils.i("data: " + mData.toString());
            });

        }
    }


    @NotNull
    @Override
    public Object getLayout() {
        return R.layout.fragment_simple_list;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
    }


    @Override
    public void initView(View view) {
        content_frame = view.findViewById(R.id.content_frame);
        mLayoutRoot = view.findViewById(R.id.layout_root);
        mHeader = view.findViewById(R.id.header);
        mRecyclerView = view.findViewById(R.id.mRecyclerView);

        mFooter = view.findViewById(R.id.footer);

        RecyclerView.LayoutManager layoutManager = getLayoutManager();
        mRecyclerView.setLayoutManager(layoutManager);

        if (isAddDividerItemDecoration) {
            RecyclerView.ItemDecoration dividerItemDecoration = addItemDecoration();
            int itemDecorationCount = mRecyclerView.getItemDecorationCount();
            if (itemDecorationCount > 0) {
                for (int i = 0; i < mRecyclerView.getItemDecorationCount(); i++) {
                    mRecyclerView.removeItemDecorationAt(i);
                }
            }
            mRecyclerView.addItemDecoration(dividerItemDecoration);
        }
        mRecyclerView.setHasFixedSize(true);

        if (mAdapter == null) {
            mAdapter = getAdapter();
            addHeaderAndFooterInList();
            mAdapter.setFooterWithEmptyEnable(true);
            mAdapter.setHeaderWithEmptyEnable(true);
//            mAdapter.setHeaderFooterEmpty(true, true);
            mAdapter.setHeaderViewAsFlow(true);
            mAdapter.setFooterViewAsFlow(true);

        }
        mRecyclerView.setAdapter(mAdapter);
        if (mAdapter.getJssRecyclerView() == null) {
            mAdapter.setRecyclerView(mRecyclerView);
//            mAdapter.bindToRecyclerView(mRecyclerView);
        }
        mAdapter.setOnItemClickListener(this);
        if (isEnableLoadMore) {
            mAdapter.getLoadMoreModule().setOnLoadMoreListener(this);
//            mAdapter.setOnLoadMoreListener(this, mRecyclerView);
        }
        mAdapter.getLoadMoreModule().setEnableLoadMore(isEnableLoadMore);

        mAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);


        mSwipeRefreshLayout = view.findViewById(R.id.mSwipeRefreshLayout);
        int color = ContextCompat.getColor(AppGlobals.getApplication(), R.color.PrimaryColor);
        mSwipeRefreshLayout.setColorSchemeColors(color);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        list_empty_view = (ViewGroup) LayoutInflater.from(_mActivity).inflate(R.layout.list_empty_view, (ViewGroup) view, false);

        mAdapter.setEmptyView(list_empty_view);
//        mAdapter.isUseEmpty(false);
    }

    protected void addHeaderAndFooterInList() {

    }

    protected JssBaseQuickAdapter<T> getAdapter() {
        return new JssBaseQuickAdapter<T>(getItemLayout(), mData) {
            @Override
            protected void convert(JssBaseViewHolder helper, T item) {
                super.convert(helper, item);
                convertItem(helper, item);
            }
        };
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(_mActivity, LinearLayoutManager.VERTICAL, false);
    }

    protected RecyclerView.ItemDecoration addItemDecoration() {
        return new DividerItemDecoration(_mActivity, LinearLayoutManager.VERTICAL);
    }

    public abstract int getItemLayout();

    public abstract void convertItem(JssBaseViewHolder helper, T item);

    public abstract Type getListType();

    protected abstract void netRequest();

    protected final void addHeader(@LayoutRes int layoutId) {
        LayoutInflater.from(_mActivity).inflate(layoutId, mHeader, true);
    }


    protected final void addContentFrame(@LayoutRes int layoutId) {
        LayoutInflater.from(_mActivity).inflate(layoutId, content_frame, true);
    }


    protected final void addEmptyView(@LayoutRes int layoutId) {
        LayoutInflater.from(_mActivity).inflate(layoutId, list_empty_view, true);

    }

    protected final void addFooter(@LayoutRes int layoutId) {
        LayoutInflater.from(_mActivity).inflate(layoutId, mFooter, true);

    }


    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

    }


    protected void parseDate(String data) throws Exception {

        Type listType = getListType();

        CommonBean<List<T>> bean = gson.fromJson(data, listType);

        if (bean == null) {
            if (pageIndex == mPrimaryPageIndex) {
                onFailed();
            } else {
                mAdapter.getLoadMoreModule().loadMoreEnd(false);
//                mAdapter.loadMoreEnd(false);
            }
            return;
        }

        List<T> contentObject = bean.getData();

        loadListDate(contentObject);

    }

    protected void loadListDate(Collection<T> collection) {


        if (collection == null || collection.size() <= 0) {
            if (pageIndex == mPrimaryPageIndex) {
                onFailed();
            } else {
                mAdapter.getLoadMoreModule().loadMoreEnd(false);
//                mAdapter.loadMoreEnd(false);
            }
            return;
        }

        if (pageIndex == mPrimaryPageIndex) {
            mAdapter.replaceData(collection);
            BaseLoadMoreModule loadMoreModule = mAdapter.getLoadMoreModule();
            loadMoreModule.setEnableLoadMoreIfNotFullPage(true);

//            mAdapter.disableLoadMoreIfNotFullPage();
        } else {
            mAdapter.addData(collection);
        }
        mAdapter.getLoadMoreModule().loadMoreComplete();
//        mAdapter.loadMoreComplete();

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
        mSwipeRefreshLayout.setEnabled(isAlwaysRefresh);
        mAdapter.getLoadMoreModule().checkDisableLoadMoreIfNotFullPage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("onViewState", "onDestroy: ");
//        CommonBean<List<T>> bean = new CommonBean<>();
//        bean.setContent(mAdapter.getData());
//        JssCacheManager.delete(TAG,bean);

        if (mAdapter != null) {
            mAdapter.clears();
        }

    }


    protected void onFailed() {
        if (mAdapter.getData().size() <= 0) {
            preEmptyView();
            mAdapter.setUseEmpty(true);
//            mAdapter.isUseEmpty(true);
        } else {
            mAdapter.getLoadMoreModule().loadMoreFail();

//            mAdapter.loadMoreFail();
        }
        mAdapter.notifyDataSetChanged();
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

    protected void preEmptyView() {
//        View reLoadButton = list_empty_view.findViewById(R.id.reLoadButton);
        TextView textView = list_empty_view.findViewById(R.id.textView);
        if (textView == null) {
            return;
        }
        if (!NetworkUtil.isNetAvailable(_mActivity)) {
            textView.setText("网络异常");
        } else {
            textView.setText("暂无数据");
        }
    }

//    @Override
//    public void onLoadMoreRequested() {
//
//        Log.d("onLoadMoreRequested", "onLoadMoreRequested: ");
//        pageIndex++;
//        mSwipeRefreshLayout.postDelayed(this::netRequest, delayedTime);
//
//    }


    @Override
    public void onLoadMore() {

        Log.d("onLoadMoreRequested", "onLoadMoreRequested: ");
        pageIndex++;
        mSwipeRefreshLayout.postDelayed(this::netRequest, delayedTime);
    }

    @Override
    public void onRefresh() {
        pageIndex = mPrimaryPageIndex;
        mAdapter.setUseEmpty(false);
//        mAdapter.isUseEmpty(false);
        mAdapter.notifyDataSetChanged();
        mAdapter.clears();
        mSwipeRefreshLayout.postDelayed(this::netRequest, delayedTime);

//        fetchData();
    }


    @Override
    public void lazyInit() {
        if (mSwipeRefreshLayout == null || mAdapter == null) return;
        mAdapter.getLoadMoreModule().setEnableLoadMore(true);
//        mAdapter.setEnableLoadMore(true);//这里的作用是防止下拉刷新的时候还可以上拉加载 todo 2.0开启
        mSwipeRefreshLayout.setRefreshing(isInitialRefresh);
        onRefresh();
    }

}
