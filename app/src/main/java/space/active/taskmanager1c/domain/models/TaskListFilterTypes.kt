package space.active.taskmanager1c.domain.models

import space.active.taskmanager1c.coreutils.addNotContainedFromList

sealed class TaskListFilterTypes {
    object IDo :
        TaskListFilterTypes() // i am performer or coperfromer and status is not Reviewed Finished Cancelled

    object IDelegate :
        TaskListFilterTypes() // i am author not performer and status is not Reviewed Finished Cancelled

    object IDidNtCheck : TaskListFilterTypes() // i am author and status is Reviewed
    object IObserve : TaskListFilterTypes() // i am observer and status is New, Accepted, Deferred
    object IDidNtRead : TaskListFilterTypes() // todo not read status
    object All : TaskListFilterTypes() // none

    companion object {
        fun List<Task>.filterIDo(user: User): List<Task> {
            val statusList =
                this.filter {  it.status != Task.Status.Reviewed && it.status != Task.Status.Finished && it.status != Task.Status.Cancelled }
            val performerList = statusList.filter { it.users.performer == user }
            val coPerformerList = statusList.filter { it.users.coPerformers.contains(user) }
            return performerList.addNotContainedFromList(coPerformerList)
        }

        fun List<Task>.filterIDelegate(user: User): List<Task> {
            val statusList =
                this.filter { it.status != Task.Status.Reviewed && it.status != Task.Status.Finished && it.status != Task.Status.Cancelled }
            return statusList.filter { it.users.author == user && it.users.performer != user && !it.users.coPerformers.contains(user) }
        }

        fun List<Task>.filterIDidNtCheck(user: User): List<Task> {
            return this.filter { it.status == Task.Status.Reviewed && it.users.author == user }
        }

        fun List<Task>.filterIObserve(user: User): List<Task> {
            val statusList =
                this.filter { it.status != Task.Status.Reviewed && it.status != Task.Status.Finished && it.status != Task.Status.Cancelled }
            return statusList.filter { it.users.observers.contains(user) }
        }
    }
}

