package space.active.taskmanager1c.presentation.screens.tasklist

import android.view.LayoutInflater
import android.view.ViewGroup
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
            taskAuthor.text = abbreviationName(task.users.author.name)
            isObserved.isVisible = task.users.observers.isNotEmpty()
            isCoPerformed.isVisible = task.users.coPerformers.isNotEmpty()
            isSending.isVisible = task.isSending
            taskStatus.isSelected = task.status == Task.Status.Finished // todo change domain model
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