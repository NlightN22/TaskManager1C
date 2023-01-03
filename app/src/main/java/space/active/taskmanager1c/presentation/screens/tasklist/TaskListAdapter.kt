package space.active.taskmanager1c.presentation.screens.tasklist

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.toShortDate
import space.active.taskmanager1c.databinding.ItemTaskBinding
import space.active.taskmanager1c.domain.models.Task

interface TaskActionListener {
    fun onTaskStatusClick(task: Task)
    fun onTaskClick(task: Task)
    fun onTaskLongClick(task: Task)
}

// TODO implement diff utils
class TaskListAdapter(
    private val actionListener: TaskActionListener
) : RecyclerView.Adapter<TasksViewHolder>(), View.OnClickListener {
    var tasks: List<Task> = emptyList()
        set(newValue) {
            val diffCallback = TasksDiffCallback(field, newValue)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newValue
            diffResult.dispatchUpdatesTo(this)
        }

    override fun onClick(v: View) {
        val task = v.tag as Task
        when (v.id) {
            R.id.taskStatus -> {
                actionListener.onTaskStatusClick(task)
            }
            else -> {
                actionListener.onTaskClick(task)
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
        val task: Task = tasks[position]
        with(holder.binding) {
            holder.itemView.tag = task           // send to onClick
            taskStatus.tag = task             // send to onClick
            taskStatus.isClickable = !task.isSending // not clickable if is sending
            taskTitle.text = task.name
            taskTitle.typeface = if (task.unread) { Typeface.DEFAULT_BOLD } else { Typeface.DEFAULT}
            taskDate.text = task.date.toShortDate()
            taskNumber.text = task.number
            taskAuthor.text = abbreviationName(
                if (task.whoIsInTask.author) {
                    task.users.performer.name
                } else {
                    task.users.author.name
                }
            )
            isObserved.isVisible = task.users.observers.isNotEmpty()
            isCoPerformed.isVisible = task.users.coPerformers.isNotEmpty()
            isSending.isVisible = task.isSending
            listItemShimmer.setSendingState(task.isSending)
            taskStatus.isVisible = task.ok
            taskStatus.isSelected = task.status == Task.Status.Reviewed
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

    //todo delete
//    private fun ImageView.setTaskStatus(status: TasKForAdapter.Status) {
//        when (status) {
//            TasKForAdapter.Status.Reviewed -> {
//                this.isSelected = true
//            }
//            TasKForAdapter.Status.NotReviewed -> {
//                this.isSelected = false
//            }
//            TasKForAdapter.Status.Invisible -> {
//                this.isVisible = false
//            }
//        }
//    }

    override fun getItemCount(): Int = tasks.size

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

class TasksViewHolder(
    val binding: ItemTaskBinding
) : RecyclerView.ViewHolder(binding.root)