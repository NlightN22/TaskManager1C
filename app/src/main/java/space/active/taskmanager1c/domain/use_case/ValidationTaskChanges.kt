package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.TaskChangesEvents
import space.active.taskmanager1c.domain.models.TaskUserIs
import space.active.taskmanager1c.domain.models.ValidationResult

class ValidationTaskChanges {

    private object Rules {
        const val TITLE_MAX_LENGTH = 120
    }

    operator fun invoke(
        changeType: TaskChangesEvents,
        userIs: TaskUserIs,
        status: Task.Status
    ): ValidationResult {
        when (changeType) {
            is TaskChangesEvents.Title -> {
                if (changeType.title.length < Rules.TITLE_MAX_LENGTH || changeType.title.isNotEmpty()) {
                    return ValidationResult.Success
                }
                return ValidationResult.Error(
                    UiText.Resource(
                        R.string.title_valid_error,
                        Rules.TITLE_MAX_LENGTH
                    )
                )
            }
            is TaskChangesEvents.EndDate -> {}
            is TaskChangesEvents.Performer -> {}
            is TaskChangesEvents.CoPerformers -> {}
            is TaskChangesEvents.Observers -> {}
            is TaskChangesEvents.Description -> {}
            is TaskChangesEvents.Status -> {
                // todo replace to resources
                when (userIs) {
                    is TaskUserIs.Author -> {
                        // author can set only Performed Finished
                        if (status == Task.Status.Performed || status == Task.Status.Finished) {
                            return ValidationResult.Success
                        }
                        return ValidationResult.Error(
                            UiText.Resource(
                                R.string.author_valid_error,
                                status.getResId(Task.Status.Performed),
                                status.getResId(Task.Status.Finished)
                            )
                        )
                    }
                    is TaskUserIs.Performer -> {
                        // performer can set only Reviewed
                        if (status == Task.Status.Reviewed) {
                            return ValidationResult.Success
                        }
                        return ValidationResult.Error(UiText.Resource(R.string.performer_valid_error, status.getResId(Task.Status.Reviewed)))
                    }
                    is TaskUserIs.PerformerInReviewed -> {
                        if (status == Task.Status.Accepted) {
                            return ValidationResult.Success
                        }
                        return ValidationResult.Error(UiText.Resource(R.string.performer_valid_error, status.getResId(Task.Status.Accepted)))
                    }
                    else -> {
                        // nothing to change
                        return ValidationResult.Error(UiText.Resource(R.string.status_valid_error))
                    }
                }
            }
        }
        return ValidationResult.Error(UiText.Resource(R.string.common_valid_error))
    }
}