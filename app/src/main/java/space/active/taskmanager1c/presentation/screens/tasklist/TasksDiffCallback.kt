package space.active.taskmanager1c.presentation.screens.tasklist

import androidx.recyclerview.widget.DiffUtil
import space.active.taskmanager1c.domain.models.TaskDomain

class  TasksDiffCallback(
    private val oldList: List<TaskDomain>,
    private val newList: List<TaskDomain>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTask = oldList[oldItemPosition]
        val newTask = newList[newItemPosition]
        return oldTask.id == newTask.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTask = oldList[oldItemPosition]
        val newTask = newList[newItemPosition]
        return oldTask == newTask
    }
}