package space.active.taskmanager1c.data.local

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import space.active.taskmanager1c.data.local.db.tasks_room_db.TaskOutputDao
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask

@ExperimentalCoroutinesApi
class OutputTaskRepositoryTest {
    @get:Rule
    val rule = MockKRule(this)

    @RelaxedMockK
    lateinit var taskOutputDao: TaskOutputDao

    @Test
    fun getTasks() = runTest {
    }

}