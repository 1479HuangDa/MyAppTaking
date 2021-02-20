package com.example.framework.recycler;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import com.example.framework.glidmodule.GlideApp;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class JssBaseViewHolder extends BaseViewHolder {
    public JssBaseViewHolder(View view) {
        super(view);
    }

    public JssBaseViewHolder setViewVisible(@IdRes int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    public JssBaseViewHolder setText(@IdRes int viewId, String value) {
        TextView view = getView(viewId);
        view.setText(value);
        return this;
    }

    public JssBaseViewHolder setRating(@IdRes int viewId, int value) {
        RatingBar view = getView(viewId);
        view.setRating(value);

        return this;
    }

    public <T extends View> T findView22(int $this$findView) {
        return super.findView($this$findView);
    }



    public JssBaseViewHolder setMaterialButtonIcon(@IdRes int viewId, @DrawableRes int drawable) {
        MaterialButton view = getView(viewId);
        view.setIconResource(drawable);
        return this;
    }

    public JssBaseViewHolder setText(@IdRes int viewId, SpannableString value) {
        TextView view = getView(viewId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        view.setText(value);
        return this;
    }

    public JssBaseViewHolder setHtmlText(@IdRes int viewId, String value) {
        TextView view = getView(viewId);
        if (value == null) value = "";
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(value, Html.FROM_HTML_MODE_LEGACY, null, null);
        } else {
            result = Html.fromHtml(value);
        }
        view.setText(result);
        return this;
    }

    public JssBaseViewHolder setMaterialCardViewStrokeColor(@IdRes int viewId, Context context,
                                                                                               @ColorRes int colorId) {
        MaterialCardView view = getView(viewId);
        view.setStrokeColor(ContextCompat.getColor(context, colorId));
        return this;
    }

    public JssBaseViewHolder setCardViewBackgroundColor(@IdRes int viewId, Context context,
                                                                                           @ColorRes int colorId) {
        CardView view = getView(viewId);
        view.setCardBackgroundColor(ContextCompat.getColor(context, colorId));
        return this;
    }

    public JssBaseViewHolder setText(@IdRes int viewId, @StringRes int strId) {
        TextView view = getView(viewId);
        view.setText(strId);
        return this;
    }

    public JssBaseViewHolder setTextGravity(@IdRes int viewId, int gravity) {
        TextView view = getView(viewId);
        view.setGravity(gravity);
        return this;
    }


    public JssBaseViewHolder setJsVisible(@IdRes int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }


    public JssBaseViewHolder setImageResource(@IdRes int viewId, @DrawableRes int imageResId) {
        ImageView view = getView(viewId);
        if (imageResId != 0) {
            view.setImageResource(imageResId);
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
        return this;
    }

    public JssBaseViewHolder setImageNetUrl(Fragment fragment, @IdRes int viewId, String url) {
        ImageView view = getView(viewId);
        GlideApp.with(fragment)
                .load(url)
                .centerCrop()
                .into(view);
        return this;
    }

    public JssBaseViewHolder setImageNetUrl(@IdRes int viewId, String url) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load(url)

                .into(view);
        return this;
    }

    public JssBaseViewHolder setImageNetUrl(@IdRes int viewId, Uri url) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load(url)
                .into(view);
        return this;
    }

    public JssBaseViewHolder setViewClick(@IdRes int viewId, View.OnClickListener clickListener) {
        View view = findView(viewId);
        if (view!=null){
            view.setOnClickListener(clickListener);
        }

        return this;
    }

    public JssBaseViewHolder setImageNetUrl22(@IdRes int viewId, String url, @DrawableRes int imageResId) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load(url)
                .placeholder(imageResId)
                .into(view);
        return this;
    }

    public JssBaseViewHolder setImageNetUrl(@IdRes int viewId, String url, @DrawableRes int imageResId) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load(url)
                .placeholder(imageResId)
                .into(view);
        return this;
    }

    public JssBaseViewHolder setImageNetUrl(
            @IdRes int viewId,
            String url,
            @DrawableRes int imageResId,
            RequestOptions mRequestOptions) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load(url)
                .apply(mRequestOptions)
                .placeholder(imageResId)
                .into(view);
        return this;
    }

//    public JssBaseViewHolder setTogglerealNameView(@IdRes int viewId, boolean visible) {
//        VerifyIconLayout view = getView(viewId);
//        view.toggleRealNameView(visible);
//        return this;
//    }
//
//    public JssBaseViewHolder setToggleProfession(@IdRes int viewId, boolean visible) {
//        VerifyIconLayout view = getView(viewId);
//        view.toggleProfessionView(visible);
//        return this;
//    }
//
//    public JssBaseViewHolder setToggleJobTitleView(@IdRes int viewId, boolean visible) {
//        VerifyIconLayout view = getView(viewId);
//        view.toggleJobTitleView(visible);
//        return this;
//    }

    public JssBaseViewHolder setImageLocalUrl(@IdRes int viewId, String url) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load("file://" + url)
                .centerCrop()
                .into(view);
        return this;
    }

    public JssBaseViewHolder setImageLocalUrl(@IdRes int viewId, String url, @DrawableRes int drawable) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load("file://" + url)
                .placeholder(drawable)
                .into(view);
        return this;
    }
//
    public JssBaseViewHolder setCircleCropImageNetUrl(@IdRes int viewId, String url) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load(url)
                .circleCrop()
                .into(view);
        return this;
    }

    public JssBaseViewHolder setCircleCropImageNetUrl(@IdRes int viewId, String url, @DrawableRes int drawable) {
        ImageView view = getView(viewId);
        GlideApp.with(view)
                .load(url)
                .circleCrop()
                .placeholder(drawable)
                .into(view);
        return this;
    }

    public JssBaseViewHolder setRatingBar(@IdRes int viewId, int rating_bar_progress) {
        RatingBar view = getView(viewId);
        view.setRating(rating_bar_progress);
        return this;
    }

    public JssBaseViewHolder setVideoThumbnail(Fragment fragment, @IdRes int viewId, String url, @DrawableRes int errorIcon) {
        ImageView view = getView(viewId);
        if (view != null) {
            GlideApp.with(fragment)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .error(errorIcon)
                    .fallback(errorIcon)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.d("wxy", "setVideoThumbnail :onLoadFailed");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("wxy", "setVideoThumbnail :onResourceReady");
                            return false;
                        }
                    })
                    .centerCrop()
                    .into(view);
        }
        return this;
    }

    public JssBaseViewHolder setRadioButtonChecked(int viewId, boolean checked) {
        RadioButton radioButton = getView(viewId);
        radioButton.setChecked(checked);
        return this;
    }

    public JssBaseViewHolder setCheckBoxChecked(int viewId, boolean checked) {
        CheckBox mCheckBox = getView(viewId);
        mCheckBox.setFocusable(false);
        mCheckBox.setChecked(checked);
        return this;
    }

    /**
     * 添加加载失败默认图
     *
     * @param fragment
     * @param viewId
     * @param url
     * @param errorIcon
     * @return
     */
    public JssBaseViewHolder setImageNetUrl(Fragment fragment, @IdRes int viewId, String url, @DrawableRes int errorIcon) {
        ImageView view = getView(viewId);
        GlideApp.with(fragment)
                .load(url)
                .error(errorIcon)
                .fallback(errorIcon)
                .centerCrop()
                .into(view);
        return this;
    }

    public JssBaseViewHolder setImageNetUrlNew(Fragment fragment, @IdRes int viewId, String url, @DrawableRes int drawable) {
        ImageView view = getView(viewId);
        GlideApp.with(fragment)
                .load(url)
                .placeholder(drawable)
                .centerCrop()
                .into(view);
        return this;
    }


    public JssBaseViewHolder setViewSelect(@IdRes int viewId, boolean select) {
        View view = getView(viewId);
        view.setSelected(select);
        return this;
    }

    public JssBaseViewHolder setViewEnabled(@IdRes int viewId, boolean select) {
        View view = getView(viewId);
//        view.setEnabled(select);
        view.setClickable(select);
        return this;
    }

    //圆形图
    public JssBaseViewHolder setCircleCropImageNetUrl(Fragment fragment, @IdRes int viewId, String url) {
        ImageView view = getView(viewId);
        if (view != null) {
            GlideApp.with(fragment)
                    .load(url)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(view);
        }
        return this;
    }

    /**
     * 添加加载失败默认图
     *
     * @param fragment
     * @param viewId
     * @param url
     * @param errorIcon
     * @return
     */

    public JssBaseViewHolder setCircleCropImageNetUrl(Fragment fragment, @IdRes int viewId, String url, @DrawableRes int errorIcon) {
        ImageView view = getView(viewId);
        if (view != null) {
            GlideApp.with(fragment)
                    .load(url)
                    .error(errorIcon)
                    .fallback(errorIcon)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .into(view);
        }
        return this;
    }

    public JssBaseViewHolder setVisible(@IdRes int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    public JssBaseViewHolder setVisibleAndInvisible(@IdRes int viewId, boolean visible) {
        View view = getView(viewId);
        view.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        return this;
    }

    public JssBaseViewHolder setTextLeftDrawable(Context context, @IdRes int viewId, @DrawableRes int drawableId) {
        Drawable drawable = context.getResources().getDrawable(drawableId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        TextView view = getView(viewId);
        view.setCompoundDrawables(drawable, null, null, null);
        return this;
    }

    public JssBaseViewHolder setTextRighttDrawable(Context context, @IdRes int viewId, @DrawableRes int drawableId) {
        Drawable drawable = context.getResources().getDrawable(drawableId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        TextView view = getView(viewId);
        view.setCompoundDrawables(null, null, drawable, null);
        return this;
    }

    public JssBaseViewHolder setProgress(@IdRes int viewId, int progress) {
        ProgressBar view = getView(viewId);
        view.setProgress(progress);
        return this;
    }

    public JssBaseViewHolder setTextRightDrawable(Context context, @IdRes int viewId, @DrawableRes int drawableId) {
        Drawable drawable = context.getResources().getDrawable(drawableId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        TextView view = getView(viewId);
        view.setCompoundDrawables(null, null, drawable, null);
        return this;
    }


//    /**
//     * Set the enabled state of this view. The interpretation of the enabled
//     * state varies by subclass.
//     *
//     * @param viewId  资源文件id
//     * @param enabled True if this view is enabled, false otherwise.
//     * @return JssBaseViewHolder
//     */
//    public JssBaseViewHolder setSwipeEnabled(@IdRes int viewId, boolean enabled) {
//        SwipeLayout view = getView(viewId);
//        if (view != null) {
//            view.setSwipeEnabled(enabled);
//        }
//        return this;
//    }

    /**
     * Set the enabled state of this view. The interpretation of the enabled
     * state varies by subclass.
     *
     * @param viewId  资源文件id
     * @param enabled True if this view is enabled, false otherwise.
     * @return JssBaseViewHolder
     */
    public JssBaseViewHolder setEnabled(@IdRes int viewId, boolean enabled) {
        View view = getView(viewId);
        if (view != null) {
            view.setEnabled(enabled);
        }
        return this;
    }

    public <T> JssBaseViewHolder addSubList(Context context, int viewId, List<T> content, final SubCovert<T> subCovert) {
        View view = getView(viewId);
        if (view instanceof ViewGroup) {
            ViewGroup tab = (ViewGroup) view;
            tab.removeAllViews();
            for (int i = 0, len = content.size(); i < len; i++) {
                View table_row = LayoutInflater.from(context).inflate(subCovert.itemLayout(), tab, false);

                final T t = content.get(i);
                subCovert.subFilled(table_row, t, i);
                table_row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        subCovert.subItemOnClickListener(t);
                    }
                });
                tab.addView(table_row);
            }
        }
        return this;
    }


//    public JssBaseViewHolder setNineImg(int viewId, List<String> pic, OnNineGridViewImageItemClickListener clickListener) {
//        NineGridView view = getView(viewId);
//        List<ImageInfo> imageInfo = new ArrayList<>();
//        for (int i = 0; i < pic.size(); i++) {
//            ImageInfo info = new ImageInfo();
//            String s = pic.get(i);
//            info.setThumbnailUrl(s);
//            info.setBigImageUrl(s);
//            imageInfo.add(info);
//        }
//        view.setAdapter(new NineGridViewAdapter(view.getContext(), imageInfo) {
//            @Override
//            protected void onImageItemClick(Context context, NineGridView nineGridView,
//                                            int index, List<ImageInfo> imageInfo) {
//                super.onImageItemClick(context, nineGridView, index, imageInfo);
//                if (clickListener != null) {
//                    clickListener.onItemClick(index, imageInfo);
//                }
//            }
//        });
//        return this;
//    }



//    public interface OnNineGridViewImageItemClickListener {
//        void onItemClick(int index, List<ImageInfo> imageInfo);
//    }

    public interface SubCovert<T> {
        void subFilled(View view, T item, int index);

        @LayoutRes
        int itemLayout();

        void subItemOnClickListener(T t);
    }
}