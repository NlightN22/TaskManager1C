package space.active.taskmanager1c.coreutils

import retrofit2.http.Body

/*
Class with all variant of exceptions in this application
 */
sealed class AppExceptions(override val message: String?): Throwable()

object AuthException: AppExceptions(message = "AuthException")

/**
 * Send object name
 */
class EmptyObject(objectName: String): AppExceptions(message = "Unexpected null or empty object: $objectName")
object ServerNoAnswer: AppExceptions(message = "ServerNoAnswer")
class NullAnswerFromServer: AppExceptions(message = "NullAnswerFromServer")
object DbUnexpectedResult: AppExceptions(message = "DbUnexpectedResult")
class ThisTaskIsNotEdited(message: String): AppExceptions(message = "ThisTaskIsNotEdited $message")
object ThisTaskIsNotNew: AppExceptions(message = "ThisTaskIsNotNew")
object TaskIsNewAndInSendingState: AppExceptions(message = "TaskIsNewAndInSendingState")
object TaskHasNotCorrectState: AppExceptions(message = "TaskHasNotCorrectState")
object JobIsNotStarted: AppExceptions(message = "JobIsNotStarted")
class CantShowSnackBar(message: String): AppExceptions(message = "Cant show snackbar $message")
class ParseBackendException(val inEx: Throwable): AppExceptions(message = "Unexpected answer from server. See log for details")
class BackendException(val errorBody: String, val errorCode: String): AppExceptions("Error answer from server $errorCode. See log for details")
class ConnectionException(val inEx: Throwable): AppExceptions("Can't connect to server. Please check your internet connection")