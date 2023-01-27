package space.active.taskmanager1c.presentation.screens.tasklist

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.toShortDate
import space.active.taskmanager1c.databinding.ItemTaskBinding
import space.active.taskmanager1c.domain.models.TaskDomain

interface TaskActionListener {
    fun onTaskStatusClick(taskDomain: TaskDomain)
    fun onTaskClick(taskDomain: TaskDomain)
    fun onTaskLongClick(taskDomain: TaskDomain)
}

// TODO implement ListAdapter
class TaskListAdapter(
    private val actionListener: TaskActionListener
) : RecyclerView.Adapter<TasksViewHolder>(), View.OnClickListener {
    var taskDomains: List<TaskDomain> = emptyList()
        set(newValue) {
            val diffCallback = TasksDiffCallback(field, newValue)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newValue
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onClick(v: View) {
        val taskDomain = v.tag as TaskDomain
        when (v.id) {
            R.id.taskStatus -> {
                actionListener.onTaskStatusClick(taskDomain)
            }
            else -> {
                actionListener.onTaskClick(taskDomain)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this)
        binding.taskStatus.setOnClickListener(this)

        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val taskDomain: TaskDomain = taskDomains[position]
        with(holder.binding) {
            holder.itemView.tag = taskDomain           // send to onClick
            taskStatus.tag = taskDomain             // send to onClick
            taskStatus.isClickable = !taskDomain.isSending // not clickable if is sending
            listItemCard.setPriority(taskDomain.priority)
            taskTitle.text = taskDomain.name
            taskTitle.typeface = if (taskDomain.unread || taskDomain.unreadTag) { Typeface.DEFAULT_BOLD } else { Typeface.DEFAULT}
            taskDate.text = taskDomain.date.toShortDate()
            taskNumber.text = taskDomain.number
            taskAuthor.text = abbreviationName(
                if (taskDomain.isAuthor) {
                    taskDomain.users.performer.name
                } else {
                    taskDomain.users.author.name
                }
            )
            isObserved.isVisible = taskDomain.users.observers.isNotEmpty()
            isCoPerformed.isVisible = taskDomain.users.coPerformers.isNotEmpty()
            isSending.isVisible = taskDomain.isSending
            listItemShimmer.setSendingState(taskDomain.isSending)
            taskStatus.isVisible = taskDomain.ok
            taskStatus.isSelected = taskDomain.status == TaskDomain.Status.Reviewed
        }
    }

    private fun ShimmerFrameLayout.setSendingState(state: Boolean) {
        if (state) {
            this.rootView.isClickable = false
            this.startShimmer()
        } else {
            this.rootView.isClickable = true
            this.stopShimmer()
        }
    }

    override fun getItemCount(): Int = taskDomains.size

    private fun abbreviationName(name: String): String {
        // split by " "
        val lines: List<String> = name.split(" ")
        val abbNameList: List<String> = lines.mapIndexed { index, s ->
            if (index != 0) {
                s.take(1) + "."
            } else {
                "$s "
            }
        }
        val abbName = abbNameList.joinToString("")
        return abbName
    }

}

private fun CardView.setPriority(priority: TaskDomain.Priority) {
    val highBackground: ColorStateList = resources.getColorStateList(R.color.high_priority, resources.newTheme())
    val lowBackground: ColorStateList = resources.getColorStateList(R.color.low_priority, resources.newTheme())
    val midBackground: ColorStateList = resources.getColorStateList(R.color.white, resources.newTheme())
    when (priority) {
        TaskDomain.Priority.High -> {this.backgroundTintList = highBackground}
        TaskDomain.Priority.Low -> {this.backgroundTintList = lowBackground}
        else -> {this.backgroundTintList = midBackground}
    }
}

class TasksViewHolder(
    val binding: ItemTaskBinding
) : RecyclerView.ViewHolder(binding.root)