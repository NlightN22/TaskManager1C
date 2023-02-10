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

class CantShowSnackBar :
    AppExceptions(text = UiText.Resource(R.string.exception_show_snackbar))

class ParseBackendException(override val message: String?, override val cause:  Throwable?) :
    AppExceptions(text = UiText.Resource(R.string.exception_parse_server_answer))

class BackendException(
    val errorBody: String,
    val errorCode: String,
    val sendToServerData: Any? = null
) :
    AppExceptions(
        UiText.Resource(
            R.string.exception_server_answer, errorCode, errorBody
        )
    ) {
    override fun equals(other: Any?): Boolean {
        if (other is BackendException) {
            return (this.errorBody == other.errorBody &&
                    this.errorCode == other.errorCode &&
                    this.sendToServerData == other.sendToServerData)
        }
        return false
    }

    override fun hashCode(): Int {
        var hash = super.hashCode()
        hash = 89 * hash + (this.errorCode.hashCode() ?: 0)
        hash = 89 * hash + (this.errorBody.hashCode() ?: 0)
        hash = 89 * hash + (this.sendToServerData?.hashCode() ?: 0)
        return hash

    }
}

object NotCorrectServerAddress :
    AppExceptions(text = UiText.Resource(R.string.exception_not_correct_URL))

class ConnectionException(val inEx: Throwable) :
    AppExceptions(UiText.Resource(R.string.exception_connection))

object FileDownloadException : AppExceptions(text = UiText.Resource(R.string.exception_file_download))

object EncryptionException : AppExceptions(text = UiText.Resource(R.string.exception_encryption))