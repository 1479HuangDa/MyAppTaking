package com.example.myapptaking.fragment.main.child.sub.chat.uitl

import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.effective.android.panel.PanelSwitchHelper


class ChatItemTouchListener(private val mHelper: PanelSwitchHelper?) : RecyclerView.OnItemTouchListener {
    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        val action = e.action
        if (action == MotionEvent.ACTION_DOWN) {
            hidePanel()

        }
        return false
    }


    private fun hidePanel() {
        mHelper?.hookSystemBackByPanelSwitcher()
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }
}