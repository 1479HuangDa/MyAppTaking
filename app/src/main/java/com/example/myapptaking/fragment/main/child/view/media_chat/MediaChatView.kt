package com.example.myapptaking.fragment.main.child.view.media_chat

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

open class MediaChatView : FrameLayout {

    companion object {
        const val CallingTo = 0

        const val Receiver = 1

    }


    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }


    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {

    }

}