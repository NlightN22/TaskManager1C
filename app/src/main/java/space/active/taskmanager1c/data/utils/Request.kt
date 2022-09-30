package space.active.taskmanager1c.data.utils


sealed class Request<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null): Request<T>(data)
    class Success<T>(data: T?): Request<T>(data)
    class Error<T>(message: String, data: T? = null): Request<T>(data, message)
}