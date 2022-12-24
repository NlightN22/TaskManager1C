package space.active.taskmanager1c.coreutils

import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun LocalDateTime.toReducedString(): String {
    return try {
        Log.d("LocalDateTime.toReducedString", "$this")
        this.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss"))
    } catch (e: Exception) {
        ""
    }

}