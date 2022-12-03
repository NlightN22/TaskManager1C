package space.active.taskmanager1c.domain.use_case

import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.Request

class HandleDbErrors(
    private val dbException: Exception
) {
    operator fun invoke(dbException: Exception): Request<String> {
        // TODO when dbException
        return PendingRequest()
    }
}