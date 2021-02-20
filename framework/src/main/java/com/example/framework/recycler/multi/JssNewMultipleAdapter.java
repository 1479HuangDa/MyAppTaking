package com.example.framework.recycler.multi;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.GridSpanSizeLookup;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.listener.OnItemChildLongClickListener;
import com.chad.library.adapter.base.module.LoadMoreModule;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;

import kotlin.Pair;

/**
 * @author HuangDa
 * create at 2020/2/29
 * description :RecyclerView 多样item布局适配器
 * @copyRights Jss
 * <p>
 * 用使用过程：
 * 先实列化, 然后 register()你的数据类型与对应的ViewHolder类型，最后再添加数据
 **/
public class JssNewMultipleAdapter<T extends JssMultiItemEntity>
        extends BaseMultiItemQuickAdapter<T, JssMultiItemViewHolder>
        implements GridSpanSizeLookup, LoadMoreModule {

    private MultiTypePool<T, JssMultiItemViewHolder> mTypePool = new MultiTypePool<>();

    private AdapterViewItemViewClick<T> adapterViewItemViewClick;

    public void setAdapterViewItemViewClick(AdapterViewItemViewClick<T> adapterViewItemViewClick) {
        this.adapterViewItemViewClick = adapterViewItemViewClick;
    }

    private LifecycleOwner lifecycleOwner;

    public void setLifecycleOwner(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public LifecycleOwner getLifecycleOwner() {
        return lifecycleOwner;
    }

    private Lifecycle mLifecycle;

    public Lifecycle getLifecycle() {
        return mLifecycle;
    }

    public void setLifecycle(Lifecycle mLifecycle) {
        this.mLifecycle = mLifecycle;
    }

    public AdapterViewItemViewClick<T> getAdapterViewItemViewClick() {
        return adapterViewItemViewClick;
    }

    private SpanSizeInterface spanSizeInterface;

    public void setSpanSizeInterface(SpanSizeInterface spanSizeInterface) {
        this.spanSizeInterface = spanSizeInterface;
    }

    public JssNewMultipleAdapter(List<T> data) {
        super(data);
        setGridSpanSizeLookup(this);

    }

    @Override
    public void onViewAttachedToWindow(@NotNull JssMultiItemViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onViewAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull JssMultiItemViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetachedFromWindow();
    }

    @Override
    public void setOnItemChildClickListener(OnItemChildClickListener listener) {
        super.setOnItemChildClickListener(listener);
    }

    @Override
    public void setOnItemChildLongClickListener(OnItemChildLongClickListener listener) {
        super.setOnItemChildLongClickListener(listener);
    }


    @Override
    protected JssMultiItemViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {

//        super.onCreateDefViewHolder(parent, viewType);

        Class<? extends JssMultiItemViewHolder> providerByClass = mTypePool.getProviderByViewType(viewType);

        try {

            Constructor<? extends JssMultiItemViewHolder> constructor = providerByClass.getDeclaredConstructor(View.class);

            constructor.setAccessible(true);

            View itemView = new View(getContext());

            JssMultiItemViewHolder tempJssMultiItemViewHolder = constructor.newInstance(itemView);

            itemView = LayoutInflater.from(getContext()).inflate(tempJssMultiItemViewHolder.getItemLayout(), parent, false);

            JssMultiItemViewHolder<T> viewHolder = constructor.newInstance(itemView);

            viewHolder.adapter = this;

            return viewHolder;

        } catch (Exception e) {
            Log.d("onCreateDefViewHolder", "onCreateDefViewHolder: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


//    @Override
//    protected void convertPayloads(@NonNull JssMultiItemViewHolder helper, T item, @NonNull List<Object> payloads) {
//        super.convertPayloads(helper, item, payloads);
//        Log.d("CommentDiffUtilCallback", "convertPayloads: "+payloads.get(0).toString());
//        helper.convert(g, item);
//    }

    @Override
    protected void convert(@NonNull JssMultiItemViewHolder helper, JssMultiItemEntity item) {

        helper.convert(getContext(), item);
    }


    public void register(@NonNull Class<? extends T> clazz, Class<? extends JssMultiItemViewHolder> provider) {
        mTypePool.register(clazz, provider);
    }


//    @Override
//    public void addData(int position, T item) {
//        Pair<Boolean, String> pair = checkRegister(item);
//        if (pair.component1()) {
//
//            super.addData(position, item);
//        } else {
//            throw new RuntimeException(pair.component2());
//        }
//    }

    private Pair<Boolean, String> checkRegister(T item) {

        Pair<Boolean, String> pair = new Pair<>(true, "");

        ArrayMap<Class<? extends T>, Class<? extends JssMultiItemViewHolder>> providers = mTypePool.getProviders();
        if (providers.size() <= 0) {

            pair = new Pair<>(false, "you must call register(..) before add(..) ");

        }

        if (providers.get(item.getClass()) == null) {

            pair = new Pair<>(false, "the ViewHolder of" + item.getClass() + "have not yet in the providers");
        }

        return pair;
    }

    @Override
    public void replaceData(@NonNull Collection<? extends T> data) {
        for (T entity : data) {
            Pair<Boolean, String> pair = checkRegister(entity);
            if (!pair.component1()) {
                throw new RuntimeException(pair.component2());
            }
        }
        super.replaceData(data);
    }

    @Override
    public void addData(@NonNull Collection<? extends T> newData) {
        for (T entity : newData) {
            Pair<Boolean, String> pair = checkRegister(entity);
            if (!pair.component1()) {
                throw new RuntimeException(pair.component2());
            }
        }

        super.addData(newData);
    }

    @Override
    public void addData(int position, @NonNull Collection<? extends T> newData) {
        for (T entity : newData) {
            Pair<Boolean, String> pair = checkRegister(entity);
            if (!pair.component1()) {
                throw new RuntimeException(pair.component2());
            }
        }
        super.addData(position, newData);
    }

    @Override
    public void addData(@NonNull T data) {

        Pair<Boolean, String> pair = checkRegister(data);
        if (pair.component1()) {
            super.addData(data);
        } else {
            throw new RuntimeException(pair.component2());
        }
    }

    @Override
    public void addData(int position, @NonNull T data) {
        Pair<Boolean, String> pair = checkRegister(data);
        if (pair.component1()) {
            super.addData(position, data);
        } else {
            throw new RuntimeException(pair.component2());
        }
    }


    public Class<? extends T> getModuleByViewType(int viewType) {
        return mTypePool.getModuleByViewType(viewType);
    }

    public void clears() {
        getData().clear();
        notifyDataSetChanged();
    }

    @Override
    protected int getDefItemViewType(int position) {

        List<T> data = getData();

        if (position < 0 || position >= data.size()) return HEADER_VIEW;

        return super.getDefItemViewType(position);
    }

    @Override
    public int getSpanSize(@NonNull GridLayoutManager manager, int viewType, int position) {

        List<T> data = getData();

        if (position < 0 || position >= data.size()) return manager.getSpanCount();

        JssMultiItemEntity mItem = data.get(position);
        if (mItem == null) return manager.getSpanCount();
        int type = getItemViewType(position);

        if (type == BaseQuickAdapter.EMPTY_VIEW
                || type == BaseQuickAdapter.HEADER_VIEW
                || type == BaseQuickAdapter.FOOTER_VIEW) {
            return manager.getSpanCount();
        } else if (spanSizeInterface != null) {
            return spanSizeInterface.getSpanSize(manager, viewType, position);
        } else {
            return 1;
        }
    }

    public interface SpanSizeInterface {
        int getSpanSize(@NonNull GridLayoutManager manager, int viewType, int position);
    }
}
