package space.active.taskmanager1c.coreutils

import android.content.Context
import java.util.*

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
            is Dynamic ->
                try {
                    value
                } catch  ( e: Throwable) {
                    e.printStackTrace()
                    "Error in getString Dynamic. See Log."
                }
            is Resource -> {
                try {
                    context.getString(resourceId, *args)
                } catch (e: UnknownFormatConversionException) {
                    context.getString(resourceId)
                } catch (e: MissingFormatArgumentException) {
                    context.getString(resourceId)
                }
            }
            is ResInRes ->
                try {
                    val listString = listRes.map { context.getString(it) }
                    context.getString(baseRes, *listString.toTypedArray())
                } catch (e: UnknownFormatConversionException) {
                    context.getString(baseRes)
                } catch (e: MissingFormatArgumentException) {
                    context.getString(baseRes)
                }
        }
    }
}