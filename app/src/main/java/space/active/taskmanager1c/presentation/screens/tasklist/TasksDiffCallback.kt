package space.active.taskmanager1c.presentation.screens.tasklist

import androidx.recyclerview.widget.DiffUtil

class  TasksDiffCallback(
    private val oldList: List<TasKForAdapter>,
    private val newList: List<TasKForAdapter>
): DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTask = oldList[oldItemPosition]
        val newTask = newList[newItemPosition]
        return oldTask.task.id == newTask.task.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldTask = oldList[oldItemPosition]
        val newTask = newList[newItemPosition]
        return oldTask == newTask
    }
}