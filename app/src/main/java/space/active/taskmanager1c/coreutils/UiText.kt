package space.active.taskmanager1c.coreutils

import android.content.Context
import java.util.UnknownFormatConversionException

private const val TAG = "UiText"

sealed class UiText {
    data class Dynamic(val value: String) : UiText()
    class Resource(
        val resourceId: Int,
        vararg val args: Any
    ) : UiText()

    class ResInRes(
        val baseRes: Int,
        val listRes: List<Int>
    ) : UiText()

    fun getString(context: Context): String {
        return when (this) {
            is Dynamic -> value
            is Resource -> {
                try {
                    context.getString(resourceId, *args)
                } catch (e: UnknownFormatConversionException) {
                    context.getString(resourceId)
                }
            }
            is ResInRes ->
                try {
                    val listString = listRes.map { context.getString(it) }
                    context.getString(baseRes, *listString.toTypedArray())
                } catch (e: UnknownFormatConversionException) {
                    context.getString(baseRes)
                }
        }
    }
}