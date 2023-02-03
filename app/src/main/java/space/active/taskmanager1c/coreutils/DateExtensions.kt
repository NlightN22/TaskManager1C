package space.active.taskmanager1c.coreutils

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.zone.ZoneOffsetTransition

fun LocalDateTime.toShortDateTime(): String {
    return try {
//        Log.d("LocalDateTime.toReducedString", "$this")
        this.format(DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss"))
    } catch (e: Exception) {
        ""
    }
}

fun ZonedDateTime.toShortDateTime(): String {
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

fun ZonedDateTime.toShortDate(): String {
    return try {
//        Log.d("LocalDateTime.toReducedString", "$this")
        this.format(DateTimeFormatter.ofPattern("dd.MM.yy"))
    } catch (e: Exception) {
        ""
    }
}

fun Long.millisecToZonedDateTime(): ZonedDateTime {
    val instant = Instant.ofEpochMilli(this)
    return ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
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

fun ZonedDateTime.nowDiffInDays(): Int {
    val today: Long = LocalDate.now().toEpochDay()
    val end: Long = this.toEpochSecond() / 60 / 60 / 24
    return (end - today).toInt()
}

/**
 * Return UTC zone by default
 * sample 2022-04-07T00:52:37
 */
fun ZonedDateTime?.toDTO(): String {
    return this?.let {
        LocalDateTime.ofInstant(it.toInstant(), ZoneOffset.UTC)
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } ?: ""
}

/**
 * Income UTC zone by default
 */
fun String.toZonedDateTime(formatter: DateTimeFormatter): ZonedDateTime {
    // 2022-04-07T00:52:37
    val currentZoneId = ZoneId.systemDefault()
    return try {
        LocalDateTime.parse(this, formatter).atOffset(ZoneOffset.UTC).atZoneSameInstant(currentZoneId)
    } catch (e: Exception) {
        LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC).atZone(currentZoneId)
    }
}

/**
 * Income UTC zone by default
 */
fun String.toZonedDateTimeOrNull(formatter: DateTimeFormatter): ZonedDateTime? {
    // 2022-04-07T00:52:37
    return try {
        val currentZoneId = ZoneId.systemDefault()
        LocalDateTime.parse(this, formatter).atOffset(ZoneOffset.UTC).atZoneSameInstant(currentZoneId)
    } catch (e: Exception) {
        null
    }
}

fun String.toLocalDateTime(formatter: DateTimeFormatter): LocalDateTime {
    // 2022-04-07T00:52:37
    return try {
        LocalDateTime.parse(this, formatter)
    } catch (e: Exception) {
        LocalDateTime.ofEpochSecond(0L, 0, ZoneOffset.UTC)
    }
}

fun String.toLocalDateTimeOrNull(formatter: DateTimeFormatter): LocalDateTime? {
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