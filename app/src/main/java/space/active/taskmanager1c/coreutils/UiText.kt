package space.active.taskmanager1c.coreutils

import android.content.Context

sealed class UiText {
    data class Dynamic(val value: String): UiText()
    class Resource(
        val resourceId: Int,
        vararg val args: Any
    ) : UiText()

    fun getString(context: Context): String{
        return when (this) {
            is Dynamic -> value
            is Resource ->  context.getString(resourceId, *args)
        }
    }
}