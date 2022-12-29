package space.active.taskmanager1c.coreutils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun LocalDateTime.toShortDateTime(): String {
    return try {
//        Log.d("LocalDateTime.toReducedString", "$this")
        this.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss"))
    } catch (e: Exception) {
        ""
    }
}

fun LocalDateTime.toShortDate(): String {
    return try {
//        Log.d("LocalDateTime.toReducedString", "$this")
        this.format(DateTimeFormatter.ofPattern("dd.MM.yy"))
    } catch (e: Exception) {
        ""
    }
}

fun Long.millisecToLocalDateTime(): LocalDateTime {
    return LocalDateTime.ofEpochSecond(
        this / 1000,
        0,
        ZoneOffset.UTC
    )
}

fun LocalDateTime.nowDiffInDays(): Int {
    val today: Long = LocalDate.now().toEpochDay()
    val end: Long = this.toEpochSecond(ZoneOffset.UTC) / 60 / 60 / 24
    return (end - today).toInt()
}