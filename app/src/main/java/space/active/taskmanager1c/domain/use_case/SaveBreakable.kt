package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.Task
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val TAG = "SaveBreakable"

class SaveBreakable @Inject constructor(
    private val logger: Logger,
    private val saveTaskChangesToDb: SaveTaskChangesToDb
) {

    var cancelListener: AtomicBoolean = AtomicBoolean(false)
    private var saveJob: Job? = null

    operator fun invoke(coroutineScope: CoroutineScope, cancelDuration: Int, task: Task) {
        saveJob?.cancel()
        saveJob = coroutineScope.launch(SupervisorJob()) {
            //set when new start
            cancelListener.set(false)
            // delay for user decision
            var timer = cancelDuration
            while (timer > 0 && !cancelListener.get()) {
                delay(1000)
                timer -= 1
            }
//            logger.log(TAG, "cancelListener.get() ${cancelListener.get()}")
            // check only after timer user decision
            if (cancelListener.get()) {
                logger.log(TAG, "save task break")
                currentCoroutineContext().cancel(null)
                cancelListener.compareAndSet(true, false)
            } else {
                // save changes to DB
                saveTaskChangesToDb(task)
            }
        }
    }
}

