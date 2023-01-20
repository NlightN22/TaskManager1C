package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.models.TaskUserIs

class GetTaskStatus {
    operator fun invoke(userIs: TaskUserIs, status: Boolean): TaskDomain.Status {
        if (userIs is TaskUserIs.AuthorInReviewed || userIs is TaskUserIs.Author) {
            // if userDomain is author and press OK set Finished
            if (status) {
                return TaskDomain.Status.Finished
            }else {
                // if userDomain is author and press Cancel set Performed
                return TaskDomain.Status.Performed
            }
        } else if (userIs is TaskUserIs.Performer && status) {
            // if userDomain is performer and press OK set Reviewed
            return TaskDomain.Status.Reviewed
        }
        // else Accepted - that's mean in work state
        return TaskDomain.Status.Accepted
    }
}