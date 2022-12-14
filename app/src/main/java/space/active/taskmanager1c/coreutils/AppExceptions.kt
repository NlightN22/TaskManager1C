package space.active.taskmanager1c.coreutils

/*
Class with all variant of exceptions in this application
 */
sealed class AppExceptions(override val message: String?): Throwable()

object AuthException: AppExceptions(message = "AuthException")
object EmptyObject: AppExceptions(message = "EmptyObject")
object ServerNoAnswer: AppExceptions(message = "ServerNoAnswer")
class NullAnswerFromServer: AppExceptions(message = "NullAnswerFromServer")
object DbUnexpectedResult: AppExceptions(message = "DbUnexpectedResult")
object ThisTaskIsNotEdited: AppExceptions(message = "ThisTaskIsNotEdited")
object ThisTaskIsNotNew: AppExceptions(message = "ThisTaskIsNotNew")
object TaskIsNewAndInSendingState: AppExceptions(message = "TaskIsNewAndInSendingState")
object TaskHasNotCorrectState: AppExceptions(message = "TaskHasNotCorrectState")
object JobIsNotStarted: AppExceptions(message = "JobIsNotStarted")