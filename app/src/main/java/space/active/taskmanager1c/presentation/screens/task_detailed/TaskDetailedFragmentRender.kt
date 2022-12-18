package space.active.taskmanager1c.presentation.screens.task_detailed

import space.active.taskmanager1c.R
import space.active.taskmanager1c.presentation.utils.setColorState
import space.active.taskmanager1c.presentation.utils.setState

fun TaskDetailedFragment.renderState(viewModel: TaskDetailedViewModel) {
    viewModel.taskState.collectOnCreate { taskState ->
        with(taskState.state) {
            binding.taskTitleDetailed.setText(title)
            binding.taskNumberDetailed.text = number
            binding.taskStatus.text = status.name
            binding.taskDateDetailed.text = startDate
            binding.taskDeadline.setText(deadLine)
            binding.taskAuthor.setText(author)
            binding.taskDaysEnd.setText(daysEnd)
            binding.taskPerformer.setText(performer)
            binding.taskCoPerformers.setText(coPerfomers)
            binding.taskObservers.setText(observers)
            binding.taskDescription.setText(description)
            binding.taskBaseObject.setText(taskObject)
            binding.taskMain.setText(mainTask)
            binding.taskInner.setText(innerTasks)
        }
    }
}

fun TaskDetailedFragment.renderFields(viewModel: TaskDetailedViewModel) {
    // Render enabled fields
    viewModel.enabledFields.collectOnCreate { fieldsState ->
        // Title
        binding.taskTitleCardView.setState(enabled = fieldsState.title)
        binding.taskTitleTIL.setState(
            enabled = fieldsState.title,
            editable = fieldsState.title
        )
        binding.taskDateDetailed.setColorState(fieldsState.title)
        binding.taskStatus.setColorState(fieldsState.title)
        binding.taskNumberDetailed.setColorState(fieldsState.title)
        // End date
        binding.taskDeadlineCardView.setState(enabled = fieldsState.deadLine)
        binding.taskDeadlineTIL.setState(enabled = fieldsState.deadLine)
        if (fieldsState.deadLine) {
            binding.taskDeadline.setOnClickListener {
                viewModel.showDatePicker()
            }
        }
        // Performer
        binding.taskPerformerCard.setState(enabled = fieldsState.performer)
        binding.taskPerformerTIL.setState(enabled = fieldsState.performer)
        if (fieldsState.performer) {
            binding.taskPerformer.setOnClickListener {
                viewModel.showDialog(PerformerDialog(null))
            }
        }
        // CoPerformers
        binding.taskCoPerformersCard.setState(enabled = fieldsState.coPerfomers)
        binding.taskCoPerformersTIL.setState(enabled = fieldsState.coPerfomers)
        if (fieldsState.coPerfomers) {
            binding.taskCoPerformers.setOnClickListener {
                viewModel.showDialog(CoPerformersDialog(null))
            }
        }
        // Observers
        binding.taskObserversCard.setState(enabled = fieldsState.observers)
        binding.taskObserversTIL.setState(enabled = fieldsState.observers)
        if (fieldsState.observers) {
            binding.taskObservers.setOnClickListener {
                viewModel.showDialog(ObserversDialog(null))
            }
        }
        // Descriptions
        binding.taskDescriptionCardView.setState(enabled = fieldsState.description)
        binding.taskDescriptionTIL.setState(
            enabled = fieldsState.description,
            editable = fieldsState.description
        )
        // bottom menu state
        binding.bottomMenu.menu.findItem(R.id.detailed_cancel).isVisible =
            fieldsState.bottomPerformer
    }
}
