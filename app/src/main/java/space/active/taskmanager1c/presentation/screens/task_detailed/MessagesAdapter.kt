package space.active.taskmanager1c.presentation.screens.task_detailed

import android.content.res.ColorStateList
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.card.MaterialCardView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.toReducedString
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
            messageTitle.text = "${item.userName}  ${item.dateTime.toReducedString()} "
            messageText.text = item.text
            messageCard.renderMyTask(item.my)
        }
    }

    override fun getItemCount(): Int = messages.size

    private fun MaterialCardView.renderMyTask(state: Boolean) {
        val myBackgroundColor: ColorStateList = resources.getColorStateList(
            R.color.my_message_background,
            resources.newTheme()
        )
        val notMyBackgroundColor: ColorStateList =
            resources.getColorStateList(R.color.editable_text_background, resources.newTheme())
        val marginParams = this.layoutParams as ViewGroup.MarginLayoutParams
        if (state) {
            marginParams.marginStart = 20
            this.foregroundGravity = Gravity.END
            this.backgroundTintList = myBackgroundColor
        } else {
            marginParams.marginStart = 0
            this.left = 0
            this.foregroundGravity = Gravity.START
            this.backgroundTintList = notMyBackgroundColor
        }
    }
}

class MessagesViewHolder(val itemViewBind: ItemMessageBinding) : ViewHolder(itemViewBind.root)