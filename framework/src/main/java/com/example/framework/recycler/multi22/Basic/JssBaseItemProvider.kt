package com.example.framework.recycler.multi22.Basic

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.chad.library.adapter.base.provider.BaseItemProvider
import com.chad.library.adapter.base.util.getItemView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.framework.recycler.multi22.JssNewMultiItemEntity
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

abstract class JssBaseItemProvider<T : JssNewMultiItemEntity?, H : BaseViewHolder> {

    lateinit var context: Context

    private var weakAdapter: WeakReference<JssBaseProviderMultiAdapter<T, H>>? = null

    private val clickViewIds by lazy(LazyThreadSafetyMode.NONE) { ArrayList<Int>() }

    private val longClickViewIds by lazy(LazyThreadSafetyMode.NONE) { ArrayList<Int>() }

    internal fun setAdapter(adapter: JssBaseProviderMultiAdapter<T, H>) {
        weakAdapter = WeakReference(adapter)
    }

    open fun getAdapter(): JssBaseProviderMultiAdapter<T, H>? {
        return weakAdapter?.get()
    }

    abstract val itemViewType: Int

    abstract val layoutId: Int
        @LayoutRes
        get

    abstract fun convert(helper: H?, item: T)

    open fun convert(helper: H, item: T, payloads: List<Any>) {}

    /**
     * （可选重写）创建 ViewHolder。
     * 默认实现返回[BaseViewHolder]，可重写返回自定义 ViewHolder
     *
     * @param parent
     */
    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
        return createBaseViewHolder(parent.getItemView(layoutId))
//        return BaseViewHolder(parent.getItemView(layoutId))
    }

    private fun createBaseViewHolder(view: View): H {
        var temp: Class<*>? = javaClass
        var z: Class<*>? = null
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp)
            temp = temp.superclass
        }
        // 泛型擦除会导致z为null
        val vh: H? = if (z == null) {
            BaseViewHolder(view) as H
        } else {
            createBaseGenericKInstance(z, view)
        }
        return vh ?: BaseViewHolder(view) as H
    }

    /**
     * try to create Generic VH instance
     *
     * @param z
     * @param view
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    private fun createBaseGenericKInstance(z: Class<*>, view: View): H? {
        try {
            val constructor: Constructor<*>
            // inner and unstatic class
            return if (z.isMemberClass && !Modifier.isStatic(z.modifiers)) {
                constructor = z.getDeclaredConstructor(javaClass, View::class.java)
                constructor.isAccessible = true
                constructor.newInstance(this, view) as H
            } else {
                constructor = z.getDeclaredConstructor(View::class.java)
                constructor.isAccessible = true
                constructor.newInstance(view) as H
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * get generic parameter VH
     *
     * @param z
     * @return
     */
    private fun getInstancedGenericKClass(z: Class<*>): Class<*>? {
        try {
            val type = z.genericSuperclass
            if (type is ParameterizedType) {
                val types = type.actualTypeArguments
                for (temp in types) {
                    if (temp is Class<*>) {
                        if (BaseViewHolder::class.java.isAssignableFrom(temp)) {
                            return temp
                        }
                    } else if (temp is ParameterizedType) {
                        val rawType = temp.rawType
                        if (rawType is Class<*> && BaseViewHolder::class.java.isAssignableFrom(
                                rawType
                            )
                        ) {
                            return rawType
                        }
                    }
                }
            }
        } catch (e: java.lang.reflect.GenericSignatureFormatError) {
            e.printStackTrace()
        } catch (e: TypeNotPresentException) {
            e.printStackTrace()
        } catch (e: java.lang.reflect.MalformedParameterizedTypeException) {
            e.printStackTrace()
        }
        return null
    }


    /**
     * （可选重写）ViewHolder创建完毕以后的回掉方法。
     * @param viewHolder VH
     */
    open fun onViewHolderCreated(viewHolder: H, viewType: Int) {}

    /**
     * Called when a view created by this [BaseItemProvider] has been attached to a window.
     * 当此[BaseItemProvider]出现在屏幕上的时候，会调用此方法
     *
     * This can be used as a reasonable signal that the view is about to be seen
     * by the user. If the [BaseItemProvider] previously freed any resources in
     * [onViewDetachedFromWindow][.onViewDetachedFromWindow]
     * those resources should be restored here.
     *
     * @param holder Holder of the view being attached
     */
    open fun onViewAttachedToWindow(holder: H) {}

    /**
     * Called when a view created by this [BaseItemProvider] has been detached from its
     * window.
     * 当此[BaseItemProvider]从屏幕上移除的时候，会调用此方法
     *
     * Becoming detached from the window is not necessarily a permanent condition;
     * the consumer of an Adapter's views may choose to cache views offscreen while they
     * are not visible, attaching and detaching them as appropriate.
     *
     * @param holder Holder of the view being detached
     */
    open fun onViewDetachedFromWindow(holder: H) {}

    /**
     * item 若想实现条目点击事件则重写该方法
     * @param helper VH
     * @param data T
     * @param position Int
     */
    open fun onClick(helper: BaseViewHolder, view: View, data: T, position: Int) {}

    /**
     * item 若想实现条目长按事件则重写该方法
     * @param helper VH
     * @param data T
     * @param position Int
     * @return Boolean
     */
    open fun onLongClick(helper: BaseViewHolder, view: View, data: T, position: Int): Boolean {
        return false
    }

    open fun onChildClick(helper: BaseViewHolder, view: View, data: T, position: Int) {}

    open fun onChildLongClick(helper: BaseViewHolder, view: View, data: T, position: Int): Boolean {
        return false
    }

    fun addChildClickViewIds(@IdRes vararg ids: Int) {
        ids.forEach {
            this.clickViewIds.add(it)
        }
    }

    fun getChildClickViewIds() = this.clickViewIds

    fun addChildLongClickViewIds(@IdRes vararg ids: Int) {
        ids.forEach {
            this.longClickViewIds.add(it)
        }
    }

    fun getChildLongClickViewIds() = this.longClickViewIds

}