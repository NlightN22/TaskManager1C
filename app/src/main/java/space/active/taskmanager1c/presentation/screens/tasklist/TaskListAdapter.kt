package space.active.taskmanager1c.presentation.screens.tasklist

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import space.active.taskmanager1c.databinding.ItemTaskBinding
import space.active.taskmanager1c.domain.models.Task

interface TaskActionListener {
    fun onTaskStatusClick(task: Task)
    fun onTaskClick(task: Task)
    fun onTaskLongClick(task: Task)
}

class TaskListAdapter: RecyclerView.Adapter<TasksViewHolder> ()
{
    var tasks: List<Task> = emptyList()
    set(newValue) {
        field = newValue
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTaskBinding.inflate(inflater, parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val task: Task = tasks[position]
        with(holder.binding) {
            taskTitle.text = task.name
            taskDate.text = task.date
            taskNumber.text = task.number
            taskAuthor.text = task.users.authorId // todo change domain model
            isObserved.isVisible = task.users.observers.isNotEmpty()
            isCoPerformed.isVisible = task.users.coPerformers.isNotEmpty()
            isSending.isVisible = task.isSending
            taskStatus.isSelected = task.status == "finished" // todo change domain model
        }
    }

    override fun getItemCount(): Int = tasks.size

}

class TasksViewHolder(
    val binding: ItemTaskBinding
) : RecyclerView.ViewHolder(binding.root)