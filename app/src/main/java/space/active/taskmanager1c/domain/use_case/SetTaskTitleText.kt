package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.databinding.TopTitleMenuBinding
import space.active.taskmanager1c.domain.models.TaskTitleViewState

fun TopTitleMenuBinding.setText(taskState: TaskTitleViewState) {
    taskNameET.setText(taskState.title)
    taskNumberTM.setText(taskState.number)
    taskDateTM.setText(taskState.date)
    taskState.status?.let { taskStatus.setText(it) }
}