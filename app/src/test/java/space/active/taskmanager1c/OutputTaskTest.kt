package space.active.taskmanager1c

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.utils.compareAndGetDiffs

class OutputTaskTest {

    @Test
    fun compareTwoDifferentInstances() {
        // arrange
        val output1: OutputTask =  mockk<OutputTask>(relaxed = true)
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