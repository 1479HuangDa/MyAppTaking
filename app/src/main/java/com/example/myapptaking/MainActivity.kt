package com.example.myapptaking

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.example.framework.base.BaseUIActivity
import com.example.myapptaking.fragment.guide.IndexFragment

class MainActivity : BaseUIActivity() {

    companion object{
        fun entry(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {

            loadRootFragment(R.id.mRoot, IndexFragment.newInstance())
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        topFragment?.onNewBundle(intent?.extras)
    }
}