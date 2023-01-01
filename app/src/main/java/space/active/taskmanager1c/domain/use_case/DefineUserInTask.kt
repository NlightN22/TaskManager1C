package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.TaskUserIs
import space.active.taskmanager1c.domain.models.User
import javax.inject.Inject


class DefineUserInTask @Inject constructor() {
    operator fun invoke(task: Task, whoAmI: User): TaskUserIs {
        if (task.users.author == whoAmI) {
            if (task.status == Task.Status.Reviewed) {
                return TaskUserIs.AuthorInReviewed()
            }
            return TaskUserIs.Author()
        } else if (task.users.performer == whoAmI || task.users.coPerformers.contains(whoAmI)) {
            //if performer in reviewed can only resume task
            if (task.status == Task.Status.Reviewed) {
                return TaskUserIs.PerformerInReviewed()
            }
            return TaskUserIs.Performer()
        }
        // todo delete
//        else if (task.users.observers.contains(whoAmI)) {
//            return TaskUserIs.Observer
//        }
        return TaskUserIs.NotAuthorOrPerformer()
    }
}
