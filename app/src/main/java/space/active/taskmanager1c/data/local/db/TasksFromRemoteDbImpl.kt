package space.active.taskmanager1c.data.local.db
//
//import android.util.Log
//import kotlinx.coroutines.*
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.flow.*
//import retrofit2.HttpException
//import space.active.taskmanager1c.data.local.db.TasksCacheRoom.TaskInputDao
//import space.active.taskmanager1c.data.local.TasksFromRemoteDb
//import space.active.taskmanager1c.data.local.db.TasksCacheRoom.entity.TaskInput
//import space.active.taskmanager1c.data.local.db.TasksCacheRoom.entity.UserInput
//import space.active.taskmanager1c.data.local.db.TasksCacheRoom.relations.TaskAndMessages
//import space.active.taskmanager1c.data.repository.TaskApi
//import space.active.taskmanager1c.data.remote.dto.MessageDto
//import space.active.taskmanager1c.data.remote.dto.TaskDto
//import space.active.taskmanager1c.data.remote.dto.TaskListDto
//import space.active.taskmanager1c.data.remote.dto.UserDto
//import space.active.taskmanager1c.data.utils.Request
//import space.active.taskmanager1c.domain.utils.Resource
//import java.io.IOException
//
//private const val REQUEST_REPEAT_DELAY = 1 * 1000L
//private const val MANUAL_UPDATE_TIME = 10 * 60 * 1000L
//private const val TAG = "TasksFromRemoteDbImpl"
//
//class TasksFromRemoteDbImpl(
//    private val dao: TaskInputDao,
//    private val api: TaskApi,
//) : TasksFromRemoteDb {
//
//    var lastUpdateTime: Long = System.currentTimeMillis()
//    private var updateError = MutableSharedFlow<String>()
//
//    override val listTasks: Flow<Request<List<TaskInput>>>
//        get() = flow {
//            emit(Request.Loading())
//            autoUpdate()
//            emit(Request.Loading())
////            val tasks = dao.getTasks()
////            emit(Request.Success(tasks))
//        }
//    override val listUsers: Flow<Request<List<UserInput>>>
//        get() = flow {
//            emit(Request.Loading())
//            autoUpdate()
//            emit(Request.Loading())
//            val users = dao.getUsers()
//            emit(Request.Success(users))
//        }
//
//    override fun getTask(taskId: String): Flow<Request<TaskAndMessages>> = flow {
//        emit(Request.Loading())
//        autoUpdate()
//        emit(Request.Loading())
//        val task = dao.getTaskAndMessages(taskId)
//        emit(Request.Success(task))
//    }
//
//    override fun getUser(userId: String): Flow<Request<UserInput>> = flow {
//        emit(Request.Loading())
//        autoUpdate()
//        emit(Request.Loading())
//        val user = dao.getUser(userId)
//        emit(Request.Success(user))
//    }
//
//    override fun updateListener(): Flow<Resource<String>> = flow {
//        while (true) {
//            emit(Resource.Loading())
////            val localTasks = dao.getTasks()
//            Log.e(TAG, "load from local")
//            delay(100L)
//            try {
//                emit(Resource.Loading())
//                Log.e(TAG, "load from remote")
//                delay(500L)
////            val remoteTasks = api.getTaskList()
////            saveTaskListDto(remoteTasks, localTasks)
//                lastUpdateTime = System.currentTimeMillis()
//                Log.e(TAG, "Last update time: $lastUpdateTime")
//            } catch (e: HttpException) {
//                updateError.emit(e.message())
//                emit(Resource.Error(data = "Not updated from remote", message = "Error"))
//            } catch (e: IOException) {
//                updateError.emit(e.message ?: "")
//            }
//            //            val localTasks = dao.getTasks()
//            Log.e(TAG, "load from local updated")
//            delay(100L)
//            emit(Resource.Success(data = "Updated from remote"))
//            Log.e(TAG, "end delay")
//            delay(1000L)
//        }
//    }.buffer(Channel.CONFLATED)
//
//    private fun isTimeToUpdate(): Boolean {
//        val currentTime = System.currentTimeMillis()
//        if (currentTime - lastUpdateTime > MANUAL_UPDATE_TIME) {
//            return true
//        }
//        return false
//    }
//
//    private fun autoUpdate() {
//
////        Log.e(TAG, "updateJob.isActive: ${updateJob.isActive}")
////        if (updateJob.isActive) {
////            updateJob.launch {
////                while (true) {
//////                    updateData()
////                    delay(REQUEST_REPEAT_DELAY)
////                }
////            }
////        }
//    }
//
//    private suspend fun updateData(): Boolean {
//        val localTasks = dao.getTasks()
//        try {
//            val remoteTasks = api.getTaskList()
//            saveTaskListDto(remoteTasks, localTasks)
//            lastUpdateTime = System.currentTimeMillis()
//            Log.e(TAG, "Last update time: $lastUpdateTime")
//            return true
//        } catch (e: HttpException) {
//            updateError.emit(e.message())
//        } catch (e: IOException) {
//            updateError.emit(e.message ?: "") // todo: add exception
//        }
//        return false
//    }
//
//    private suspend fun saveTaskListDto(tasksRemote: TaskListDto, tasksLocal: List<TaskInput>) {
//        // Load all Task from Db
//        val tasksLocalId = tasksLocal.map { it.id }
//        if (tasksLocalId.isNotEmpty()) {
//            // Find and delete not input tasks
//            tasksLocalId.forEach { localId ->
//                if (tasksRemote.tasks.map { it.id }.notContainsId(localId)) {
//                    dao.deleteTask(localId)
//                }
//            }
//            // Find and delete not contained users
//            val allUsers = getAllUsersIdInTasks()
//            allUsers.forEach { userId ->
//                if (tasksRemote.users.map { it.id }.notContainsId(userId)) {
//                    dao.deleteUser(userId)
//                }
//            }
//        }
//        // Save input tasks
//        saveTasks(tasksRemote.tasks)
//        saveUsers(tasksRemote.users)
//    }
//
//    private suspend fun getAllUsersIdInTasks(): List<String> {
//        val tasks = dao.getTasks()
//        val allUsers: List<String> =
//            tasks.map { it.authorId } + tasks.map { it.performerId } + tasks.flatMap { it.coPerformers }
//        return allUsers
//    }
//
//    private fun List<String>.notContainsId(localId: String): Boolean {
//        if (localId !in this) {
//            return true
//        }
//        return false
//    }
//
//    private suspend fun saveTasks(tasksDto: List<TaskDto>) {
//        try {
////            Log.e(TAG, "${tasksDto.map { it.toTaskDb()}}")
//            dao.insertTask(tasksDto.map { it.toTaskDb() })
//            tasksDto.forEach { task ->
//                saveMessages(task.messages, taskId = task.id)
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error saveTasks: ${e.message}")
//        }
//    }
//
//    private suspend fun saveUsers(users: List<UserDto>) {
//        try {
//            dao.insertUser(users.map { it.toUserDb() })
//        } catch (e: Exception) {
//            Log.e(TAG, "Error saveUsers: ${e.message}")
//        }
//    }
//
//    private suspend fun saveMessages(messages: List<MessageDto>, taskId: String) {
//        try {
//            dao.insertMessage(messages.map { it.toMessageDb(taskId) })
//        } catch (e: Exception) {
//            Log.e(TAG, "Error saveUsers: ${e.message}")
//        }
//    }
//}