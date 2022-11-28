package space.active.taskmanager1c.coreutils

import java.lang.RuntimeException

sealed class AppExceptions: RuntimeException()

object AuthException: AppExceptions()
object EmptyObject: AppExceptions()

