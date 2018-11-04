package de.zweidenker.iotp2ptest.util

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView

class NoScrollListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ListView(context, attrs, defStyleAttr) {
    override fun scrollTo(x: Int, y: Int) { }
}