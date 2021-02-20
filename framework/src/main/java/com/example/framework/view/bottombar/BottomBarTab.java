package com.example.framework.view.bottombar;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.example.framework.R;
import com.example.framework.global.AppGlobals;


public class BottomBarTab extends FrameLayout {
    private ImageView mIcon;

    private TextView mTvTitle;
    private int mTabPosition = -1;
    private TextView mTvUnreadCount;

    private Object pageTag ;

    public Object getPageTag() {
        return pageTag;
    }

    public BottomBarTab setPageTag(Object pageTag) {
        this.pageTag = pageTag;
        return this;
    }

    public ImageView getIcon() {
        return mIcon;
    }

    public BottomBarTab(Context context) {
        this(context, null);
    }

    public BottomBarTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBarTab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.bottom_bar_tab_layout, this);

        mIcon = findViewById(R.id.tab_icon);

        mTvTitle = findViewById(R.id.tab_title);

        mTvUnreadCount = findViewById(R.id.unreadTv);
    }

    public BottomBarTab setContent(@StringRes int title, @DrawableRes int icon) {
        mTvTitle.setText(title);
        mIcon.setImageResource(icon);
        contentSelect(isSelected());
        return this;
    }


    /**
     * 设置未读数量
     */
    public void setUnreadCount(int num) {
        if (num <= 0) {
            mTvUnreadCount.setText(String.valueOf(0));
            mTvUnreadCount.setVisibility(GONE);
        } else {
            mTvUnreadCount.setVisibility(VISIBLE);
            if (num > 99) {
                mTvUnreadCount.setText("99+");
            } else {
                mTvUnreadCount.setText(String.valueOf(num));
            }
        }
    }

    /**
     * 获取当前未读数量
     */
    public int getUnreadCount() {
        int count = 0;
        if (TextUtils.isEmpty(mTvUnreadCount.getText())) {
            return count;
        }
        if (mTvUnreadCount.getText().toString().equals("99+")) {
            return 99;
        }
        try {
            count = Integer.valueOf(mTvUnreadCount.getText().toString());
        } catch (Exception ignored) {
        }
        return count;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        contentSelect(selected);
    }

    private void contentSelect(boolean selected) {
        Context mContext = AppGlobals.getApplication();
        if (selected) {
            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.PrimaryColor));
            mTvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.PrimaryColor));
        } else {
            mIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.text_color_22));
            mTvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.text_color_22));
        }
    }


    public void setTabPosition(int position) {
        mTabPosition = position;
        if (position == 0) {
            setSelected(true);
        }
    }

    public int getTabPosition() {
        return mTabPosition;
    }
}