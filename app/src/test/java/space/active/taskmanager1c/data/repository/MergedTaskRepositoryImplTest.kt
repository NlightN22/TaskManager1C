package space.active.taskmanager1c.data.repository

import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.InputTaskRepository
import space.active.taskmanager1c.data.local.OutputTaskRepository
import space.active.taskmanager1c.data.local.db.tasks_room_db.FilterType
import space.active.taskmanager1c.data.local.db.tasks_room_db.InputTaskRepositoryImpl
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.data.local.db.tasks_room_db.output_entities.OutputTask
import space.active.taskmanager1c.data.repository.utils.*
import space.active.taskmanager1c.domain.models.TaskListFilterTypes
import space.active.taskmanager1c.domain.models.TaskListOrderTypes
import space.active.taskmanager1c.domain.repository.TasksRepository
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class MergedTaskRepositoryImplTest : TestCase() {


    private lateinit var tasksRepository: TasksRepository

    @RelaxedMockK
    private lateinit var inputTaskRepository: InputTaskRepository

    @RelaxedMockK
    private lateinit var outputTaskRepository: OutputTaskRepository

    @get:Rule
    val rule = MockKRule(this)

    public override fun setUp() {
        super.setUp()
        tasksRepository = createTasksRepository()
    }


    fun testGetInputTasksCount() = runTest {
        val expectedCount = 1000
        val inputTaskRepository = createInputTaskRepository()
        tasksRepository = createTasksRepository(inputTaskRepository = inputTaskRepository)

        coEvery { inputTaskRepository.getInputTasksCount() } returns expectedCount

        val count = tasksRepository.getInputTasksCount()

        coVerifySequence {
            inputTaskRepository.listUsersFlow
            inputTaskRepository.getInputTasksCount()
        }
        assertEquals(expectedCount, count)
    }

    fun testGetTasksFiltered() = runTest {
        val expectedInputTasks: Flow<List<TaskInputHandledWithUsers>> = flow {  emit(listOf(createTaskInputHandledWithUsers())) }
        val expectedUsers: List<UserInput> = createListUserInput()
        val expectedResult = expectedInputTasks.first().map { it.toTaskDomain(expectedUsers) }
        val filterTypes: Flow<TaskListFilterTypes> = flow { emit(createTaskListFilterTypes()) }
        val orderTypes: Flow<TaskListOrderTypes> = flow { emit(createTaskListOrderTypes()) }
        val myIdCur = createAuthorId()
        val myIdFlow: Flow<String> = flow { emit(myIdCur) }
        val sortField = orderTypes.first().getSortFieldAndType().first
        val sortType = orderTypes.first().getSortFieldAndType().second
        val filterType: FilterType = filterTypes.first().toFilterType()

        val inputTaskRepository = createInputTaskRepository()
        val outputTaskRepository = createOutputTaskRepository()
        tasksRepository = createTasksRepository(inputTaskRepository, outputTaskRepository)

        coEvery { inputTaskRepository.sortedQuery(myIdCur, filterType, sortField, sortType) } returns expectedInputTasks

        coEvery { inputTaskRepository.getUsers() } returns expectedUsers

        coEvery { outputTaskRepository.outputTaskList } returns flow { emit(emptyList<OutputTask>()) }

        coEvery { inputTaskRepository.getUnreadIds() } returns flow { emit(emptyList()) }

        val result = tasksRepository.getTasksFiltered(filterTypes,orderTypes,myIdFlow)

        assertEquals(expectedResult, result.first())
    }

    fun testGetTasksFilteredWithOutput() = runTest {
        val expectedInputTasks: Flow<List<TaskInputHandledWithUsers>> = flow {  emit(listOf(createTaskInputHandledWithUsers())) }
        val expectedUsers: List<UserInput> = createListUserInput()
        val expectedResult = expectedInputTasks.first().map { it.toTaskDomain(expectedUsers) }
        val filterTypes: Flow<TaskListFilterTypes> = flow { emit(createTaskListFilterTypes()) }
        val orderTypes: Flow<TaskListOrderTypes> = flow { emit(createTaskListOrderTypes()) }
        val myIdCur = createAuthorId()
        val myIdFlow: Flow<String> = flow { emit(myIdCur) }
        val sortField = orderTypes.first().getSortFieldAndType().first
        val sortType = orderTypes.first().getSortFieldAndType().second
        val filterType: FilterType = filterTypes.first().toFilterType()

        val inputTaskRepository = createInputTaskRepository()
        val outputTaskRepository = createOutputTaskRepository()
        tasksRepository = createTasksRepository(inputTaskRepository, outputTaskRepository)

        coEvery { inputTaskRepository.sortedQuery(myIdCur, filterType, sortField, sortType) } returns expectedInputTasks

        coEvery { inputTaskRepository.getUsers() } returns expectedUsers

        // todo replace output
        coEvery { outputTaskRepository.outputTaskList } returns flow { emit(emptyList<OutputTask>()) }

        coEvery { inputTaskRepository.getUnreadIds() } returns flow { emit(emptyList()) }

        val result = tasksRepository.getTasksFiltered(filterTypes,orderTypes,myIdFlow)

        assertEquals(expectedResult, result.first())
    }

    fun testGetInnerTasks() {}

    fun testGetListUsersFlow() {}

    fun testGetTask() {}

    fun testEditTask() {}

    fun testCreateNewTask() {}

    private fun createLogger(): Logger {
        val logger: Logger = mockk()
        every { logger.log(any(), any()) } just runs
        return logger
    }

    private fun createInputTaskRepository(): InputTaskRepository {
        val inputTaskRepository: InputTaskRepositoryImpl = mockk()
        coEvery { inputTaskRepository.listUsersFlow } returns mockk()
        return inputTaskRepository
    }

    private fun createOutputTaskRepository(): OutputTaskRepository = mockk()

    private fun createTasksRepository(
        inputTaskRepository: InputTaskRepository = createInputTaskRepository(),
        outputTaskRepository: OutputTaskRepository = createOutputTaskRepository(),
        ioDispatcher: CoroutineDispatcher = createIODispatcher(),
        logger: Logger = createLogger()
    ): TasksRepository =
        MergedTaskRepositoryImpl(
            inputTaskRepository,
            outputTaskRepository,
            ioDispatcher,
            logger
        )

    private fun createIODispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

}