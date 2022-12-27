package space.active.taskmanager1c.coreutils

import space.active.taskmanager1c.R

/*
Class with all variant of exceptions in this application
 */
sealed class AppExceptions(val text: UiText) : Throwable()

object AuthException : AppExceptions(text = UiText.Resource(R.string.exception_auth))

/**
 * Send object name
 */

class EmptyObject(objectName: String) :
    AppExceptions(text = UiText.Resource(R.string.exception_empty_object, objectName))

object DbUnexpectedResult : AppExceptions(text = UiText.Resource(R.string.exception_db))
class ThisTaskIsNotEdited(text: String) :
    AppExceptions(text = UiText.Resource(R.string.exception_not_editable, text))

object ThisTaskIsNotNew : AppExceptions(text = UiText.Resource(R.string.exception_not_new))
object TaskIsNewAndInSendingState :
    AppExceptions(text = UiText.Resource(R.string.exception_is_sending))

object TaskHasNotCorrectState :
    AppExceptions(text = UiText.Resource(R.string.exception_not_correct_status))

class CantShowSnackBar:
    AppExceptions(text = UiText.Resource(R.string.exception_show_snackbar, ))

class ParseBackendException(val inEx: Throwable) :
    AppExceptions(text = UiText.Resource(R.string.exception_parse_server_answer))

class BackendException(val errorBody: String, val errorCode: String) : AppExceptions(
    UiText.Resource(
        R.string.exception_server_answer, errorCode
    )
)

class ConnectionException(val inEx: Throwable) :
    AppExceptions(UiText.Resource(R.string.exception_connection))