package space.active.taskmanager1c.coreutils


/**
 * Base class which represents result of some async operation
 */
sealed class Request<T>

/**
 * Operation is in progress
 */
class PendingRequest<T> : Request<T>()

/**
 * Operation has finished successfully
 */
class SuccessRequest<T>(
    val data: T
) : Request<T>()

/**
 * Operation has finished with error
 */
class ErrorRequest<T>(
    val exception: Throwable
) : Request<T>()

/**
 * Get success value of [Request] if it is possible; otherwise return NULL.
 */
fun <T> Request<T>?.takeSuccess(): T? {
    return if (this is SuccessRequest)
        this.data
    else
        null
}