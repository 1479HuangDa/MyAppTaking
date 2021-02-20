package com.example.framework.recycler.multi22.Basic

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.framework.base.BaseUIFragment
import com.example.framework.recycler.multi22.JssNewMultiItemEntity

abstract class JssBaseProviderMultiAdapter<T : JssNewMultiItemEntity?, H : BaseViewHolder>(data: MutableList<T>? = null) :
    BaseQuickAdapter<T, H>(0, data), LoadMoreModule {


    private var fragment: BaseUIFragment? = null

    private val mItemProviders by lazy(LazyThreadSafetyMode.NONE) { SparseArray<JssBaseItemProvider<T, H>>() }


    fun attachFragment(fragment: BaseUIFragment?) {
        this.fragment = fragment

    }

    fun detachFragment() {
        fragment = null
    }

    fun getAttachFragment() = fragment


    /**
     * 返回 item 类型
     * @param data List<T>
     * @param position Int
     * @return Int
     */
    protected abstract fun getItemType(data: List<T>, position: Int): Int

    /**
     * 必须通过此方法，添加 provider
     * @param provider BaseItemProvider
     */
    open fun addItemProvider(provider: JssBaseItemProvider<T, H>) {
        provider.setAdapter(this)
        mItemProviders.put(provider.itemViewType, provider)
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): H {
        val provider = getItemProvider(viewType)
        checkNotNull(provider) { "ViewType: $viewType no such provider found，please use addItemProvider() first!" }
        provider.context = parent.context
        return provider.onCreateViewHolder(parent, viewType).apply {
            provider.onViewHolderCreated(this, viewType)
        }
    }

    override fun getDefItemViewType(position: Int): Int {
        return getItemType(data, position)
    }

    override fun convert(holder: H, item: T) {
        getItemProvider(holder.itemViewType)!!.convert(holder, item)
    }

    override fun convert(holder: H, item: T, payloads: List<Any>) {
        getItemProvider(holder.itemViewType)!!.convert(holder, item, payloads)
    }

    override fun bindViewClickListener(viewHolder: H, viewType: Int) {
        super.bindViewClickListener(viewHolder, viewType)
        bindClick(viewHolder)
        bindChildClick(viewHolder, viewType)
    }

    /**
     * 通过 ViewType 获取 BaseItemProvider
     * 例如：如果ViewType经过特殊处理，可以重写此方法，获取正确的Provider
     * （比如 ViewType 通过位运算进行的组合的）
     *
     * @param viewType Int
     * @return BaseItemProvider
     */
    protected open fun getItemProvider(viewType: Int): JssBaseItemProvider<T, H>? {
        return mItemProviders.get(viewType)
    }

    override fun onViewAttachedToWindow(holder: H) {
        super.onViewAttachedToWindow(holder)
        getItemProvider(holder.itemViewType)?.onViewAttachedToWindow(holder)
    }

    override fun onViewDetachedFromWindow(holder: H) {
        super.onViewDetachedFromWindow(holder)
        getItemProvider(holder.itemViewType)?.onViewDetachedFromWindow(holder)
    }

    protected open fun bindClick(viewHolder: H) {
        if (getOnItemClickListener() == null) {
            //如果没有设置点击监听，则回调给 itemProvider
            //Callback to itemProvider if no click listener is set
            viewHolder.itemView.setOnClickListener {
                var position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }
                position -= headerLayoutCount

                val itemViewType = viewHolder.itemViewType
                val provider = mItemProviders.get(itemViewType)

                provider.onClick(viewHolder, it, data[position], position)
            }
        }
        if (getOnItemLongClickListener() == null) {
            //如果没有设置长按监听，则回调给itemProvider
            // If you do not set a long press listener, callback to the itemProvider
            viewHolder.itemView.setOnLongClickListener {
                var position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnLongClickListener false
                }
                position -= headerLayoutCount

                val itemViewType = viewHolder.itemViewType
                val provider = mItemProviders.get(itemViewType)
                provider.onLongClick(viewHolder, it, data[position], position)
            }
        }
    }

    protected open fun bindChildClick(viewHolder: BaseViewHolder, viewType: Int) {
        if (getOnItemChildClickListener() == null) {
            val provider = getItemProvider(viewType) ?: return
            val ids = provider.getChildClickViewIds()
            provider.setAdapter(this)
            ids.forEach { id ->
                viewHolder.itemView.findViewById<View>(id)?.let {
                    if (!it.isClickable) {
                        it.isClickable = true
                    }
                    it.setOnClickListener { v ->
                        var position: Int = viewHolder.adapterPosition

                        position -= headerLayoutCount

                        if (position == RecyclerView.NO_POSITION) {
                            return@setOnClickListener
                        }
                        provider.onChildClick(viewHolder, v, data[position], position)
                    }
                }
            }
        }
        if (getOnItemChildLongClickListener() == null) {
            val provider = getItemProvider(viewType) ?: return
            val ids = provider.getChildLongClickViewIds()
            ids.forEach { id ->
                viewHolder.itemView.findViewById<View>(id)?.let {
                    if (!it.isLongClickable) {
                        it.isLongClickable = true
                    }
                    it.setOnLongClickListener { v ->
                        var position: Int = viewHolder.adapterPosition
                        if (position == RecyclerView.NO_POSITION) {
                            return@setOnLongClickListener false
                        }
                        position -= headerLayoutCount
                        provider.onChildLongClick(viewHolder, v, data[position], position)
                    }
                }
            }
        }
    }
}