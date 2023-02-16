package space.active.taskmanager1c.coreutils

import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.view.forEach

fun View.getAllViews(): List<View> {
    return if (this !is ViewGroup || this.childCount == 0) {
        listOf(this)
    } else {
        val listViews = arrayListOf<View>()
        listViews.add(this)
        this.forEach {
            if (it is ViewGroup) {
                listViews.addAll(it.getAllViews())
            } else {
                listViews.add(it)
            }
        }
        listViews
    }
}

fun View.setSwipeListener(
    actionUp: () -> Unit, actionDown: () -> Unit,
    scrollView: ScrollView? = null
) {
    this.setOnTouchListener(object :
        OnSwipeTouchListener(this.context, scrollView) {
        override fun onSwipeUp() {
            super.onSwipeUp()
            actionUp()
        }

        override fun onSwipeDown() {
            super.onSwipeDown()
            actionDown()
        }

    }
    )
}

fun View.setSwipeListener(
    actionUp: () -> Unit,
    actionDown: () -> Unit,
    actionClick: () -> Unit,
    scrollView: ScrollView? = null
) {
    this.setOnTouchListener(object :
        OnSwipeTouchListener(this.context, scrollView) {
        override fun onSwipeUp() {
            actionUp()
            super.onSwipeUp()
        }

        override fun onSwipeDown() {
            actionDown()
            super.onSwipeDown()
        }

        override fun onClick() {
            actionClick()
            super.onClick()
        }
    }
    )
}