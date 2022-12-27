package space.active.taskmanager1c.domain.use_case

import kotlinx.coroutines.*
import space.active.taskmanager1c.domain.models.Task
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SaveDelayed @Inject constructor(
    private val saveTaskChangesToDb: SaveTaskChangesToDb
) {

    private val delayedJobsList: ArrayList<SaveJob> = arrayListOf()

    operator fun invoke(coroutineScope: CoroutineScope, key: String, task: Task, delay: Int) {
        val newJob: SaveJob = SaveJob(context = CoroutineName(key))
//                logger.log(TAG, "Input list: $delayedJobsList")
        if (delayedJobsList.map { it.context }.contains(newJob.context)) {
            val filteredContext = delayedJobsList.filter { it.context == newJob.context }
            filteredContext.forEach {
//                        logger.log(TAG, "ToCancel list: $delayedJobsList")
                it.job?.cancel()
            }
        }
//                logger.log(TAG, "Started list: $delayedJobsList")
        // all cancellable jobs must have supervisor for work
        newJob.job = coroutineScope.launch(newJob.context + SupervisorJob()) {
            // Add new job if list not contains same key
            if (!delayedJobsList.map { it.context }.contains(newJob.context)) {
                delayedJobsList.add(newJob)
            } else {
                // or update job if we have it
                delayedJobsList.map {
                    if (it.context == newJob.context) {
                        it.job = newJob.job
                    } else {
                        it
                    }
                }
            }
//                    logger.log(TAG, "Final list: $delayedJobsList")
            delay((delay * 1000).toLong())
            delayedJobsList.remove(newJob)
//                    logger.log(TAG, "Removed list: $delayedJobsList")
//                    logger.log(TAG, "Task to save: $task")
            saveTaskChangesToDb(task)
        }
    }

    data class SaveJob(
        var job: Job? = null,
        val context: CoroutineContext
    )
}