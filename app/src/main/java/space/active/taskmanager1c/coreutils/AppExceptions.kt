package space.active.taskmanager1c.coreutils

import java.lang.RuntimeException

/*
Class with all variant of exceptions in this application
 */
sealed class AppExceptions: IllegalStateException()

object AuthException: AppExceptions()
object EmptyObject: AppExceptions()
object ServerNoAnswer: AppExceptions()
class NullAnswerFromServer: AppExceptions()
object DbUnexpectedResult: AppExceptions()