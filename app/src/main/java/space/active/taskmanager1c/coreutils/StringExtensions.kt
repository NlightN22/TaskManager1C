package space.active.taskmanager1c.coreutils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun String.toDateTime(formatter: DateTimeFormatter): LocalDateTime {
    // 2022-04-07T00:52:37
    return try {
        LocalDateTime.parse(this, formatter)
    } catch (e: Exception) {
        LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC)
    }
}

fun String.toDateTimeOrNull(formatter: DateTimeFormatter): LocalDateTime? {
    // 2022-04-07T00:52:37
    return try {
        LocalDateTime.parse(this, formatter)
    } catch (e: Exception) {
        null
    }
}


fun String.toDate(formatter: DateTimeFormatter): LocalDate {
    // 2022-04-07T00:52:37
    return try {
        LocalDate.parse(this, formatter)
    } catch (e: Exception) {
        LocalDate.ofEpochDay(0L)
    }
}