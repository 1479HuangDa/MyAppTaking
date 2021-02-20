package com.example.framework.view;

import android.content.Context;
import android.util.AttributeSet;

public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

    public MarqueeTextView(Context context) {
        super(context);

    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean isFocused() {

        return true;
    }

}