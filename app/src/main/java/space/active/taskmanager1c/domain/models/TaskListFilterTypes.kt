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
        fun List<TaskDomain>.filterIDo(): List<TaskDomain> {
            return this.filter { it.isPerformer }
        }

        fun List<TaskDomain>.filterIDelegate(): List<TaskDomain> {
            return this.filter { it.isAuthor && !it.isPerformer && !it.cancel }
       }

        fun List<TaskDomain>.filterIDidNtCheck(): List<TaskDomain> {
            return this.filter { it.isAuthor && it.cancel }
       }

        fun List<TaskDomain>.filterIObserve(): List<TaskDomain> {
           return this.filter { !it.isAuthor && !it.isPerformer }
        }

        fun List<TaskDomain>.filterUnread() = this.filter { it.unread }
    }
}

