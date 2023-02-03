package space.active.taskmanager1c

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.utils.compareAndGetDiffs
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class OutputTaskTest {


    @Test
    fun dateToDTO() {
        val domainStringDate: String = "2023-01-25T14:12:31+07:00[Asia/Krasnoyarsk]"
        val domainDate: ZonedDateTime? = ZonedDateTime.parse(domainStringDate)
        val convertedDate: String =
            domainDate?.let { LocalDateTime.ofInstant(it.toInstant(), ZoneOffset.UTC).toString() }
                ?: ""
        println("convertedDate: $convertedDate")

    }


    @Test
    fun compareTwoVersions() {
        // AAAAAE/ljW8= AAAAAE+qB9s= AAAAAEz53N8=
        val version1 = "AAAAAEz53N8="
        val version2 = "AAAAAE053N8="
//        val version2 = "AAAAAFBsKWQ="
        println("version1 > version2: ${version1 > version2}")
        println("version1 <= version2: ${version1 <= version2}")
    }

    @Test
    fun getZoneTime() {
        //2022-04-07T00:52:37
        val dateTime = "2022-04-07T00:52:37"
        val curTimeZone = TimeZone.getDefault().rawOffset
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val zoneId = ZoneId.systemDefault()
        println(zoneId.toString())
        val zoneOffset = ZonedDateTime.now().offset
        println(zoneOffset.toString())
        val currentLocal = LocalDateTime.parse(dateTime, formatter)
        println(currentLocal.toString())
        val zonedDateTime = ZonedDateTime.of(currentLocal, ZoneOffset.UTC)
        println(zonedDateTime.toString())
        val offsetTime = LocalDateTime.parse(dateTime, formatter).atOffset(ZoneOffset.UTC)
        println(offsetTime.toString())
        val final = offsetTime.atZoneSameInstant(zoneId)
        println(final.toString())
        val toDTO = final.toLocalDateTime().toString()
        println(toDTO)

    }


    @Test
    fun compareTwoDifferentInstances() {
        // arrange
        val output1: OutputTask = mockk<OutputTask>(relaxed = true)
        val output2: OutputTask = mockk<OutputTask>(relaxed = true)

        with(mockk<OutputTask>()) {
            every {
                output1.compareAndGetDiffs(output2)
            } returns mapOf<String, String>(Pair("A", "A"))
        }

        // act
        val outputDiffMap = output1.compareAndGetDiffs(output2)

        // assert

        println(outputDiffMap.toString())
//        verify {
//
//        }

    }
}