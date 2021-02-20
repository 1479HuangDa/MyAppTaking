package com.example.framework.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import org.jetbrains.annotations.NotNull;

public abstract class BaseLazyDataBindingFragment<T extends ViewDataBinding> extends LazyFragment {

    protected T viewBinder;


    public abstract @LayoutRes
    int layoutId();

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int mLayoutId = layoutId();
        viewBinder = DataBindingUtil.inflate(getLayoutInflater(), mLayoutId, container, false);
        return viewBinder.getRoot();
    }


    @NotNull
    @Override
    public Object getLayout() {
        return layoutId();
    }
}