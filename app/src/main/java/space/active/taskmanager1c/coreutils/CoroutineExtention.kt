package space.active.taskmanager1c.coreutils

import kotlinx.coroutines.*

fun CoroutineScope.resume (appDispatchers: AppDispatchers) {

    val coroutineContext =
        SupervisorJob() + appDispatchers + CoroutineExceptionHandler { context, exception -> }
// TODO delete or impl
    if (!this.isActive) {
        this.newCoroutineContext(coroutineContext)
//        val tmp = AppDispatchers()


    }
}