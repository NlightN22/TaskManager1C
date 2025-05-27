package space.active.taskmanager1c.coreutils

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import kotlin.math.abs

internal open class OnSwipeTouchListener(c: Context?, private val scrollView: ScrollView? = null) :
    View.OnTouchListener {
    private val gestureDetector: GestureDetector
    var mStartY: Float? = null

    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
//        Log.d("OnSwipeTouchListener", "motionEvent: $motionEvent")
        scrollView?.let { scroll ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                mStartY = motionEvent.y
            }
//            Log.d("OnSwipeTouchListener", "mStart: $mStartY")
            mStartY?.let { startY ->
                // If move down and scroll on top position
                if (onMoveDown(startY, motionEvent.y)) {
//                    Log.d("OnSwipeTouchListener", "onMoveDown")
                    if (!scroll.canScrollVertically(-1)) {
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        return gestureDetector.onTouchEvent(motionEvent)
                    }
                }
                // If move down and scroll on bop position
                else if (onMoveUp(startY, motionEvent.y)) {
//                    Log.d("OnSwipeTouchListener", "onMoveUp")
                    if (!scroll.canScrollVertically(1)) {
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        return gestureDetector.onTouchEvent(motionEvent)
                    }
                }
            }
        }
        return gestureDetector.onTouchEvent(motionEvent)
    }

    private fun onMoveDown(mStartY: Float, mMoveY: Float): Boolean {
        return mStartY < mMoveY
    }

    private fun onMoveUp(mStartY: Float, mMoveY: Float): Boolean {
        return mStartY > mMoveY
    }

    private inner class GestureListener() :
        GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD: Int = 100
        private val SWIPE_VELOCITY_THRESHOLD: Int = 100
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (e1 == null) return false
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(
                            velocityX
                        ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                    }
                } else {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(
                            velocityY
                        ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffY < 0) {
                            onSwipeUp()
                        } else {
                            onSwipeDown()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }
    }


    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    open fun onClick() {}
    private fun onDoubleClick() {}
    private fun onLongClick() {}

    init {
        gestureDetector = GestureDetector(c, GestureListener())
    }
}