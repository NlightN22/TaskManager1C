package space.active.taskmanager1c.data.repository.utils

import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.CoPerformersInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.ObserversInTask
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.TaskInputHandled
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.UserInput
import space.active.taskmanager1c.data.local.db.tasks_room_db.input_entities.relations.TaskInputHandledWithUsers
import space.active.taskmanager1c.domain.models.TaskListFilterTypes
import space.active.taskmanager1c.domain.models.TaskListOrderTypes
import kotlin.random.Random

fun createTaskListOrderTypes() = TaskListOrderTypes.Name(true)

fun createTaskListFilterTypes() = TaskListFilterTypes.IDo

fun createTaskInputHandledWithUsers() = TaskInputHandledWithUsers(
    taskInput = createTaskInput(),
    coPerformers = listOf(createCoPerformersInTask()),
    observers = listOf(createObserversInTask())
)

fun createTaskInput() = TaskInputHandled (
    date = "testDate",
    description = "testDescription",
    deadline = "testDeadline",
    id = "testTaskId",
    mainTaskId = "testMainTaskId",
    name = "testName",
    number = "testNumber",
    objName = "testObjName",
    priority = "middle",
    status = "performed",
    authorId = createAuthorId(),
    performerId = "testPerformerId",
    unreadTag = false,
    version = 10000,
    isAuthor = true,
    isPerformer = true,
    ok = true,
    cancel = true
        )

fun createCoPerformersInTask() = CoPerformersInTask(
    coPerformerId = "testCoperformerId",
    taskId = "testTaskId"
)

fun createObserversInTask() = ObserversInTask(
    observerId = "testObserverId",
    taskId = "testTaskId"
)

fun createListUserInput() = listOf<UserInput>(UserInput(
    userId = createAuthorId(),
    userName = "testName"
))

fun createAuthorId() = "testAuthorId"