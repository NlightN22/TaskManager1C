package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.databinding.TopTitleMenuBinding
import space.active.taskmanager1c.presentation.utils.setColorState
import space.active.taskmanager1c.presentation.utils.setState

fun TopTitleMenuBinding.setState(editable: Boolean) {
        taskTitleCardView.setState(enabled = editable)
        taskTitleTIL.setState(enabled = editable)

        taskDateTM.setColorState(editable)
        taskStatus.setColorState(editable)
        taskNumberTM.setColorState(editable)
    }
