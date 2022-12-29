package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.TaskUserIs

class GetTaskStatus {
    operator fun invoke(userIs: TaskUserIs, status: Boolean): Task.Status {
        if (userIs is TaskUserIs.AuthorInReviewed || userIs is TaskUserIs.Author) {
            // if user is author and press OK set Finished
            if (status) {
                return Task.Status.Finished
            }else {
                // if user is author and press Cancel set Performed
                return Task.Status.Performed
            }
        } else if (userIs is TaskUserIs.Performer && status) {
            // if user is performer and press OK set Reviewed
            return Task.Status.Reviewed
        }
        // else Accepted - that's mean in work state
        return Task.Status.Accepted
    }
}