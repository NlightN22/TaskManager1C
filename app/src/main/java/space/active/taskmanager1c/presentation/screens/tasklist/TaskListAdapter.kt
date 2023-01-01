package space.active.taskmanager1c.presentation.screens.tasklist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.toShortDate
import space.active.taskmanager1c.databinding.ItemTaskBinding
import space.active.taskmanager1c.domain.models.Task

interface TaskActionListener {
    fun onTaskStatusClick(task: Task)
    fun onTaskClick(task: Task)
    fun onTaskLongClick(task: Task)
}
data class TasKForAdapter(
    val task: Task,
    val status: Status
){
    enum class Status {
        Reviewed,
        NotReviewed,
        Invisible
    }
}


// TODO implement diff utils
class TaskListAdapter(
    private val actionListener: TaskActionListener
) : RecyclerView.Adapter<TasksViewHolder>(), View.OnClickListener {
    var tasks: List<TasKForAdapter> = emptyList()
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
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
        val taskAdapter: TasKForAdapter = tasks[position]
        with(holder.binding) {
            holder.itemView.tag = taskAdapter.task           // send to onClick
            taskStatus.tag = taskAdapter.task             // send to onClick
            taskTitle.text = taskAdapter.task.name
            taskDate.text = taskAdapter.task.date.toShortDate()
            taskNumber.text = taskAdapter.task.number
            taskAuthor.text = abbreviationName(taskAdapter.task.users.author.name)
            isObserved.isVisible = taskAdapter.task.users.observers.isNotEmpty()
            isCoPerformed.isVisible = taskAdapter.task.users.coPerformers.isNotEmpty()
            isSending.isVisible = taskAdapter.task.isSending
            taskStatus.setTaskStatus(taskAdapter.status)
        }
    }

    private fun ImageView.setTaskStatus(status: TasKForAdapter.Status) {
        when (status) {
            TasKForAdapter.Status.Reviewed ->  {
                this.isSelected = true
                this.isVisible = true
            }
            TasKForAdapter.Status.NotReviewed ->  {
                this.isSelected = false
                this.isVisible = true
            }
            TasKForAdapter.Status.Invisible ->  {
                this.isVisible = false
            }
        }
    }

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