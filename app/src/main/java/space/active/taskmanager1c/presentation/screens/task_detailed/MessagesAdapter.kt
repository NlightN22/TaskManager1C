package space.active.taskmanager1c.presentation.screens.task_detailed

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.card.MaterialCardView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.toShortDateTime
import space.active.taskmanager1c.databinding.ItemMessageBinding
import space.active.taskmanager1c.domain.models.Messages

class MessagesAdapter : RecyclerView.Adapter<MessagesViewHolder>() {
    var messages: List<Messages> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMessageBinding.inflate(inflater, parent, false)
        return MessagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        val item = messages[position]
        with(holder.itemViewBind) {
            messageTitle.text = "${item.userName}  ${item.dateTime.toShortDateTime()} "
            messageText.typeface = if (item.unread) {
                Typeface.DEFAULT_BOLD} else {
                Typeface.DEFAULT}
            messageText.text = item.text
            messageCard.renderMyTask(item.my)
            messageConstraint.renderMyTask(item.my)
            setFlagState(item.my)
        }
    }

    override fun getItemCount(): Int = messages.size

    private fun ItemMessageBinding.setFlagState(state: Boolean) {
        if (state) {
            messageImageMy.visibility = View.VISIBLE
            messageImageNotMy.visibility = View.GONE
        }else {
            messageImageMy.visibility = View.GONE
            messageImageNotMy.visibility = View.VISIBLE
        }
    }

    private fun ConstraintLayout.renderMyTask(state: Boolean) {
        val layoutParams = this.layoutParams as RecyclerView.LayoutParams
        val marginDp = 20F
        val marginPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDp, this.context.resources.displayMetrics).toInt()
        if (state) {
            layoutParams.marginStart = marginPx
            layoutParams.marginEnd = 0
        }else {
            layoutParams.marginStart = 0
            layoutParams.marginEnd = marginPx
        }
    }

    private fun MaterialCardView.renderMyTask(state: Boolean) {
        val myBackgroundColor: ColorStateList = resources.getColorStateList(
            R.color.my_message_background,
            resources.newTheme()
        )
        val notMyBackgroundColor: ColorStateList =
            resources.getColorStateList(R.color.editable_text_background, resources.newTheme())
        val layoutParams = this.layoutParams as ConstraintLayout.LayoutParams
        if (state) {
            layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            this.backgroundTintList = myBackgroundColor
        } else {
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
            this.backgroundTintList = notMyBackgroundColor
        }
    }
}

class MessagesViewHolder(val itemViewBind: ItemMessageBinding) : ViewHolder(itemViewBind.root)