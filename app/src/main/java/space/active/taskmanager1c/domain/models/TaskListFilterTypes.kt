package space.active.taskmanager1c.domain.models

sealed class TaskListFilterTypes {
    object IDo :
        TaskListFilterTypes() // i am performer or coperfromer and status is not Reviewed Finished Cancelled

    object IDelegate :
        TaskListFilterTypes() // i am author not performer and status is not Reviewed Finished Cancelled

    object IDidNtCheck : TaskListFilterTypes() // i am author and status is Reviewed
    object IObserve : TaskListFilterTypes() // i am observer and status is New, Accepted, Deferred
    object IDidNtRead : TaskListFilterTypes() // unread status
    object All : TaskListFilterTypes() // none

    companion object {
        fun List<Task>.filterIDo(): List<Task> {
            return this.filter { it.whoIsInTask.performer }
        }

        fun List<Task>.filterIDelegate(): List<Task> {
            return this.filter { it.whoIsInTask.author && !it.whoIsInTask.performer && !it.cancel }
       }

        fun List<Task>.filterIDidNtCheck(): List<Task> {
            return this.filter { it.whoIsInTask.author && it.cancel }
       }

        fun List<Task>.filterIObserve(): List<Task> {
           return this.filter { !it.whoIsInTask.author && !it.whoIsInTask.performer }
        }

        fun List<Task>.filterUnread() = this.filter { it.unread }
    }
}

