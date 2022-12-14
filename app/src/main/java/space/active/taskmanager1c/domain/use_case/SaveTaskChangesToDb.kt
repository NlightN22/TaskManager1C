package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request
import space.active.taskmanager1c.coreutils.SaveRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.di.IoDispatcher
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.repository.TasksRepository
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val TAG = "SaveTaskChangesToDb"
private const val DELAY_TO_SAVE = 5 // seconds

class SaveTaskChangesToDb @Inject constructor(
    private val repository: TasksRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger
) {
    var cancelListener: AtomicBoolean = AtomicBoolean(false)

    operator fun invoke(cancelDuration: Int, task: Task): Flow<Request<Any>> = flow {
        //set when new start
        cancelListener.set(false)
        // delay for user decision
        var timer = cancelDuration
        while (timer > 0 && !cancelListener.get()) {
            emit(SaveRequest(timer))
            delay(1000)
            timer -= 1
        }

        // check only after timer user decision
        if (cancelListener.get()) {
            currentCoroutineContext().cancel(null)
            cancelListener.compareAndSet(true, false)
        }

        emit(PendingRequest())
//        repository.editTask(task)
        delay(5000L) // todo delete
        logger.log(TAG, "Task saved: $task")
        emit(SuccessRequest(Any()))
    }.flowOn(ioDispatcher)

    fun cancelSave() {
        cancelListener.set(true)
    }
}