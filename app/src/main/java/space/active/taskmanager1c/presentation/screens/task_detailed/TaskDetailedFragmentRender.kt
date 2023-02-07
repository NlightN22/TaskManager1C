package space.active.taskmanager1c.presentation.screens.task_detailed

import space.active.taskmanager1c.R
import space.active.taskmanager1c.presentation.utils.setColorState
import space.active.taskmanager1c.presentation.utils.setState

fun TaskDetailedFragment.renderState(viewModel: TaskDetailedViewModel) {
    viewModel.taskState.collectOnCreated { taskState ->
        with(taskState) {
            binding.taskTitleDetailed.setText(title)
            binding.taskNumberDetailed.text = number
            binding.taskStatus.text = resources.getString(status.getResId())
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
    viewModel.enabledFields.collectOnCreated { fieldsState ->
        // Title
        binding.taskTitleCardView.setState(enabled = fieldsState.title)
        binding.taskTitleTIL.setState(enabled = fieldsState.title)

        binding.taskDateDetailed.setColorState(fieldsState.title)
        binding.taskStatus.setColorState(fieldsState.title)
        binding.taskNumberDetailed.setColorState(fieldsState.title)
        // End date
        binding.taskDeadlineCardView.setState(enabled = fieldsState.deadLine)
        binding.taskDeadlineTIL.setState(enabled = fieldsState.deadLine)
        // Performer
        binding.taskPerformerCard.setState(enabled = fieldsState.performer)
        binding.taskPerformerTIL.setState(enabled = fieldsState.performer)
        // CoPerformers
        binding.taskCoPerformersCard.setState(enabled = fieldsState.coPerfomers)
        binding.taskCoPerformersTIL.setState(enabled = fieldsState.coPerfomers)

        // Observers
        binding.taskObserversCard.setState(enabled = fieldsState.observers)
        binding.taskObserversTIL.setState(enabled = fieldsState.observers)
        // Descriptions
        binding.taskDescriptionCardView.setState(enabled = fieldsState.description)
        binding.taskDescriptionTIL.setState(enabled = fieldsState.description)

        // bottom menu state items
        binding.bottomMenu.root.apply {
            menu.clear()
            if (fieldsState.bottomNew) {
                inflateMenu(R.menu.menu_save_cancel)
            } else {
                inflateMenu(R.menu.menu_detailed)
                menu.findItem(R.id.detailed_ok).isVisible =
                    fieldsState.bottomOk
                menu.findItem(R.id.detailed_cancel).isVisible =
                    fieldsState.bottomCancel
                menu.findItem(R.id.detailed_messages).isVisible = true
            }
        }
    }
}

