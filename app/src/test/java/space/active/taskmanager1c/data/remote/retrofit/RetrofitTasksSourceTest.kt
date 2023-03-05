package space.active.taskmanager1c.data.remote.retrofit

import com.google.gson.Gson
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Retrofit
import space.active.taskmanager1c.coreutils.BackendException
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.remote.model.AuthBasicDto
import space.active.taskmanager1c.data.remote.model.TaskDto
import space.active.taskmanager1c.data.remote.model.TaskListDto
import space.active.taskmanager1c.data.remote.model.UserDto
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskMessagesDTO
import space.active.taskmanager1c.data.remote.model.messages_dto.TaskUserReadingFlagDTO
import space.active.taskmanager1c.data.remote.model.reading_times.FetchReadingTimes
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesDTO
import space.active.taskmanager1c.data.remote.model.reading_times.ReadingTimesTaskDTO
import space.active.taskmanager1c.data.remote.model.reading_times.SetReadingTimeDTO
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class RetrofitTasksSourceTest {

    @get:Rule
    val rule = MockKRule(this)

    @RelaxedMockK
    lateinit var retrofitApi: RetrofitApi

    private lateinit var retrofitTasksSource: RetrofitTasksSource

    private val inputAuth = createAuthDTO()

    @Before
    fun setUp() {
        retrofitTasksSource = createRetrofitTaskSource()
    }

    @Test
    fun authUserAndReturnSuccess() = runTest {
        val expectedAuthResponse = successAuthResponse()

        coEvery { retrofitTasksSource.authUser(inputAuth) } returns expectedAuthResponse

        val auth = retrofitTasksSource.authUser(inputAuth)

        assertSame(expectedAuthResponse, auth)
        coVerify(exactly = 1) {
            retrofitTasksSource.authUser(inputAuth)
        }
        confirmVerified(retrofitApi)
    }

    @Test
    fun authUserAndReturnError() = runTest {
        val expectedException = BackendException(
            errorBody = "testBody",
            errorCode = "500",
            sendToServerData = "Test data"
        )

        coEvery { retrofitApi.authUser(any()) } throws expectedException

        val exception: BackendException = catchException {
            retrofitTasksSource.authUser(inputAuth)
        }

        coVerify(exactly = 1) {
            retrofitApi.authUser(inputAuth.toBasic())
        }
        confirmVerified(retrofitApi)
        assertSame(expectedException, exception)
    }

    @Test
    fun getTaskListFromServer() = runTest {
        val expectedTaskList = TaskListDto()

        coEvery { retrofitApi.getTasks(inputAuth.toBasic()) } returns expectedTaskList

        val tasks = retrofitTasksSource.getTaskList(inputAuth)

        coVerify(exactly = 1) {
            retrofitApi.getTasks(inputAuth.toBasic())
        }
        confirmVerified(retrofitApi)
        assertSame(expectedTaskList, tasks)
    }

    @Test
    fun sendNewTaskToServer() = runTest {
        val expectedResponse = TaskListDto(
            tasks = listOf(mockk<TaskDto>()),
            users = listOf(mockk<UserDto>())
        )
        val expectedAnswer = expectedResponse.tasks.first()
        val newTask = mockk<TaskDto>()

        coEvery { retrofitApi.sendNew(inputAuth.toBasic(), newTask) } returns expectedResponse

        val response = retrofitTasksSource.sendNewTask(inputAuth, newTask)

        coVerify(exactly = 1) {
            retrofitApi.sendNew(inputAuth.toBasic(), newTask)
        }
        confirmVerified(retrofitApi)
        if (response is SuccessRequest) {
            assertSame(expectedAnswer, response.data)
        } else {
            throw AssertionError()
        }
    }

    @Test
    fun sendEditedTaskToServer() = runTest {
        val gson: Gson = mockk()
        val retrofitTasksSource = createRetrofitTaskSource(
            gson = gson
        )
        val expectedResponse = TaskListDto(
            tasks = listOf(mockk<TaskDto>()),
            users = listOf(mockk<UserDto>())
        )
        val expectedAnswer = expectedResponse.tasks.first()
        val taskId = "testId"
        val taskChanges = mapOf("testParam" to "Value")

        every { gson.toJson(taskChanges) } returns ""

        val changes = gson.toJson(taskChanges)

        coEvery {
            retrofitApi.saveChanges(
                taskId,
                inputAuth.toBasic(),
                changes
            )
        } returns expectedResponse

        val response =
            retrofitTasksSource.sendEditedTaskMappedChanges(inputAuth, taskId, taskChanges)

        coVerify(exactly = 1) {
            retrofitApi.saveChanges(taskId, inputAuth.toBasic(), changes)
        }
        confirmVerified(retrofitApi)
        assertSame(expectedAnswer, response)
    }

    @Test
    fun getMessagesFromServer()  = runTest {
        val expectedAnswer: TaskMessagesDTO = mockk()
        val testTaskId = "testID"

        coEvery { retrofitApi.getMessages(inputAuth.toBasic(),testTaskId) } returns expectedAnswer

        val response = retrofitTasksSource.getMessages(inputAuth, testTaskId)

        coVerify(exactly = 1) {
            retrofitApi.getMessages(inputAuth.toBasic(), testTaskId)
        }
        confirmVerified(retrofitApi)
        assertSame(expectedAnswer, response)
    }

    @Test
    fun sendMessagesToServer() = runTest {
        val expectedAnswer: TaskMessagesDTO = mockk()
        val testTaskId = "testID"
        val testText = "testText"
        coEvery {
            retrofitApi.sendMessages(inputAuth.toBasic(), mapOf("id" to testTaskId, "text" to testText))
        } returns expectedAnswer

        val response = retrofitTasksSource.sendMessage(inputAuth, testTaskId, testText)

        coVerify(exactly = 1) {
            retrofitApi.sendMessages(inputAuth.toBasic(), mapOf("id" to testTaskId, "text" to testText))
        }
        confirmVerified(retrofitApi)
        assertSame(expectedAnswer, response)
    }

    @Test
    fun getMessagesTimesFromServer() = runTest {
        val expectedAnswer: ReadingTimesDTO = ReadingTimesDTO(listOf<ReadingTimesTaskDTO>(mockk()))
        val testTaskListId: List<String> = mockk()
        val fetchReadingTimes = FetchReadingTimes(testTaskListId)

        coEvery {
            retrofitApi.getReadingTimes(inputAuth.toBasic(), fetchReadingTimes)
        } returns expectedAnswer

        val response = retrofitTasksSource.getMessagesTimes(inputAuth, testTaskListId)

        coVerify(exactly = 1) {
            retrofitApi.getReadingTimes(inputAuth.toBasic(), fetchReadingTimes)
        }
        confirmVerified(retrofitApi)
        assertSame(expectedAnswer.Tasks, response)
    }

    @Test
    fun setReadingTimeOnServer() = runTest {
        val expectedAnswer: ReadingTimesTaskDTO = mockk()
        val testTaskId: String = "testId"
        val testMessageTime: LocalDateTime = mockk()
        val testReadingTime: LocalDateTime = mockk()
        val requestToServer: SetReadingTimeDTO = SetReadingTimeDTO(
            id = testTaskId,
            messageTime = testMessageTime.toString(),
            readTime = testReadingTime.toString())

        coEvery {
            retrofitApi.setReadingTime(inputAuth.toBasic(), requestToServer)
        } returns expectedAnswer

        val response = retrofitTasksSource.setReadingTime(inputAuth, testTaskId, testMessageTime, testReadingTime)

        coVerify(exactly = 1) {
            retrofitApi.setReadingTime(inputAuth.toBasic(), requestToServer)
        }
        confirmVerified(retrofitApi)
        assertSame(expectedAnswer, response)
    }

    @Test
    fun setReadingFlagOnServer() = runTest {
        val expectedAnswer: TaskUserReadingFlagDTO = mockk()
        val testTaskId: String = "testId"
        val testFlag: Boolean = true
        val requestToServer = mapOf<String, String>("id" to testTaskId, "flag" to testFlag.toString())

        coEvery {
            retrofitApi.setReadingFlag(inputAuth.toBasic(), requestToServer)
        } returns expectedAnswer

        val response = retrofitTasksSource.setReadingFlag(inputAuth, testTaskId, testFlag)

        coVerify(exactly = 1) {
            retrofitApi.setReadingFlag(inputAuth.toBasic(), requestToServer)
        }
        confirmVerified(retrofitApi)
        assertSame(expectedAnswer, response)
    }

    private fun createAuthDTO() = AuthBasicDto(
        name = "username",
        pass = "password"
    )

    private fun successAuthResponse() = UserDto(
        id = "testID",
        name = "testName"
    )

    private fun createRetrofitTaskSource(
        logger: Logger = createLogger(),
        gson: Gson = createGson(),
        retrofit: Retrofit = createRetrofit()
    ): RetrofitTasksSource {
        return RetrofitTasksSource(logger, gson, retrofit)
    }

    private fun createGson(): Gson {
        val gson: Gson = mockk()
        every { gson.toJson(Any()) } returns ""
        return gson
    }

    private fun createLogger(): Logger {
        val logger: Logger = mockk()
        every { logger.log(any(), any()) } just runs
        return logger
    }

    private fun createRetrofit(): Retrofit {
        val retrofit = mockk<Retrofit>()
        every { retrofit.create(RetrofitApi::class.java) } returns retrofitApi
        return retrofit
    }

    inline fun <reified T : Throwable> catchException(block: () -> Unit): T {
        try {
            block()
        } catch (e: Throwable) {
            if (e is T) {
                return e
            } else {
                Assert.fail(
                    "Invalid exception type. " +
                            "Expected: ${T::class.java.simpleName}, " +
                            "Actual: ${e.javaClass.simpleName}"
                )
            }
        }
        throw AssertionError("No expected exception")
    }
}