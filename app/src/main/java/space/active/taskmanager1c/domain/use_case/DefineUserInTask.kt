package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.models.TaskUserIs
import space.active.taskmanager1c.domain.models.UserDomain
import javax.inject.Inject


class DefineUserInTask @Inject constructor() {
    operator fun invoke(taskDomain: TaskDomain, whoAmI: UserDomain): TaskUserIs {
        if (taskDomain.users.author == whoAmI) {
            if (taskDomain.status == TaskDomain.Status.Reviewed) {
                return TaskUserIs.AuthorInReviewed()
            }
            if (taskDomain.status != TaskDomain.Status.Finished) {
                return TaskUserIs.Author()
            }
        } else if (taskDomain.users.performer == whoAmI || taskDomain.users.coPerformers.contains(whoAmI)) {
            //if performer in reviewed can only resume taskDomain
            if (taskDomain.status == TaskDomain.Status.Reviewed) {
                return TaskUserIs.PerformerInReviewed()
            }
            if (taskDomain.status != TaskDomain.Status.Finished) {
                return TaskUserIs.Performer()
            }
        }
        return TaskUserIs.NotAuthorOrPerformer()
    }
}
