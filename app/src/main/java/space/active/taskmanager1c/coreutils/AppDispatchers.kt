package space.active.taskmanager1c.coreutils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers



abstract class AppDispatchers: CoroutineDispatcher() {
    companion object{
        val io: CoroutineDispatcher = Dispatchers.IO
    }
}


