package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.domain.models.Task

class ValidationTaskChanges(

) {
    operator fun invoke(
        changeType: TaskChangesEvents,
        userIs: TaskUserIs,
        status: Task.Status
    ): ValidationResult {
        when (changeType) {
            is TaskChangesEvents.Title -> {}
            is TaskChangesEvents.EndDate -> {}
            is TaskChangesEvents.Performer -> {}
            is TaskChangesEvents.CoPerformers -> {}
            is TaskChangesEvents.Observers -> {}
            is TaskChangesEvents.Description -> {}
            is TaskChangesEvents.Status -> {
                when (userIs) {
                    is TaskUserIs.Author -> {
                        // author can set only Performed Finished
                        if (status == Task.Status.Performed || status == Task.Status.Finished) {
                            return ValidationResult.Success
                        }
                        return ValidationResult.Error("author can set only Performed Finished")
                    }
                    is TaskUserIs.Performer -> {
                        // performer can set only Reviewed
                        if (status == Task.Status.Reviewed) {
                            return ValidationResult.Success
                        }
                        return ValidationResult.Error("performer can set only Reviewed")
                    }
                    is TaskUserIs.NotAuthorOrPerformer -> {
                        // nothing to change
                        return ValidationResult.Error("Not author or performer")
                    }
                }
            }
        }
        return ValidationResult.Error("No such type of changes")
    }
}