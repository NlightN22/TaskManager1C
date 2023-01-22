package space.active.taskmanager1c.coreutils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import space.active.taskmanager1c.coreutils.NotifyingLinearLayoutManager.OnLayoutCompleteCallback


fun RecyclerView.getPositionsAfterLoading(block: (first: Int, last: Int) -> Unit) {
    val lm =
        this.layoutManager
    lm?.let {
        if (it is NotifyingLinearLayoutManager) {
            it.mCallback = object : OnLayoutCompleteCallback {
                override fun onLayoutComplete() {
                    val first = it.findFirstCompletelyVisibleItemPosition()
                    val last = it.findLastCompletelyVisibleItemPosition()
                    block(first, last)
                }
            }
        }
    }
}

fun RecyclerView.getPositionsAfterScroll(block: (first: Int, last: Int) -> Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                val lm: LinearLayoutManager? = recyclerView.layoutManager as LinearLayoutManager?
                lm?.let {
                    val first = it.findFirstCompletelyVisibleItemPosition()
                    val last = it.findLastCompletelyVisibleItemPosition()
                    block(first, last)
                }
            }
            super.onScrollStateChanged(recyclerView, newState)
        }
    }
    )
}

/**
 * This class calls [mCallback] (instance of [OnLayoutCompleteCallback]) when all layout
 * calculations are complete, e.g. following a call to
 * [RecyclerView.Adapter.notifyDataSetChanged()] (or related methods).
 *
 * In a paginated listing, we will decide if load more needs to be called in the said callback.
 */
class NotifyingLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    var mCallback: OnLayoutCompleteCallback? = null

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        if (isLastItemCompletelyVisible()) {
            mCallback?.onLayoutComplete()
        }
    }

    private fun isLastItemCompletelyVisible() = findLastCompletelyVisibleItemPosition() != -1

    interface OnLayoutCompleteCallback {
        fun onLayoutComplete()
    }
}