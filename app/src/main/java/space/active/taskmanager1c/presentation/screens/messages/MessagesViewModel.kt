package space.active.taskmanager1c.presentation.screens.messages

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.Messages
import space.active.taskmanager1c.domain.models.Messages.Companion.toMessages
import space.active.taskmanager1c.domain.models.UserDomain
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.*
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import java.time.LocalDateTime
import javax.inject.Inject

private const val TAG = "MessagesViewModel"

@HiltViewModel
class MessagesViewModel @Inject constructor(
    settings: SettingsRepository,
    logger: Logger,
    private val getTaskMessages: GetTaskMessages,
    private val sendTaskMessages: SendTaskMessages,
    private val exceptionHandler: ExceptionHandler,
    private val setTaskAndMessageReadingTime: SetTaskAndMessageReadingTime,
    private val getCredentials: GetCredentials,
    private val tasksRepository: TasksRepository,
) : BaseViewModel(settings, logger) {

    private val _messagesViewState = MutableStateFlow(MessagesViewState())
    val messagesViewState = _messagesViewState.asStateFlow()

    private val _sendMessageEvent = MutableSharedFlow<StateProgress<Any>>()
    val sendMessageEvent = _sendMessageEvent.asSharedFlow()

    private val _messagesList = MutableStateFlow<Request<List<Messages>>>(PendingRequest())
    val messageList = _messagesList.asStateFlow()

    private val whoAmI: Flow<UserDomain> = settings.getUserFlow()

    private val _inputTaskId = MutableStateFlow<String>("")

    private val _showSnackBar = MutableSharedFlow<UiText>()
    val showSnackBar = _showSnackBar.asSharedFlow()

    var updateJob: Job? = null
    fun successLogin(taskId: String) {
        _inputTaskId.value = taskId
        // start auto update job
        updateJob?.cancel()
        logger.log(TAG, "successLogin taskId: $taskId")
        updateJob = viewModelScope.launch {
            while (true) {
                showMessages(taskId)
                delay(2000) //messages update delay
            }
        }
        viewModelScope.launch {
            tasksRepository.getTask(taskId).collectLatest { task ->
                task?.let {
                    _messagesViewState.value = _messagesViewState.value.copy(
                        title = it.name,
                        date = it.date.toShortDate(),
                        number = it.number,
                        status = it.status.getResId()
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val curTaskId = _inputTaskId.first()
            if (curTaskId.isNotBlank()) {
                sendTaskMessages(getCredentials(), curTaskId, text).collect { res ->
                    when (res) {
                        is PendingRequest -> {
                            _sendMessageEvent.emit(Loading())
                        }
                        is SuccessRequest -> {
//                                logger.log(TAG, res.data.messages.toString())
                            showMessages(curTaskId)
                            _sendMessageEvent.emit(Success(Any()))
                        }
                        else -> {
                            _sendMessageEvent.emit(Success(Any()))
                        }
                    }
                }
            } else {
                _showSnackBar.emit(UiText.Resource(R.string.error_new_task_send_message))
            }
        }
    }

    private suspend fun showMessages(taskId: String) {
        getTaskMessages(getCredentials(), taskId).collect { request ->
            when (request) {
                is PendingRequest -> {}
                is ErrorRequest -> {
                    exceptionHandler(request.exception)
                }
                is SuccessRequest -> {
                    val messagesList = request.data.messages
                    val convertedMessages =
                        messagesList.toMessages(request.data.users, request.data.readingTime)
                            .sortedBy { it.dateTime }
                    val setMyList: List<Messages> = convertedMessages.map {
                        if (it.authorId == whoAmI.first().id) {
                            it.copy(my = true)
                        } else {
                            it.copy(my = false)
                        }
                    }
                    val currentList = _messagesList.value
                    if (currentList is SuccessRequest) {
                        if (currentList.data != setMyList) {
                            updateUI(taskId, setMyList)
                        }
                    } else {
                        updateUI(taskId, setMyList)
                    }
                }
            }
        }
    }

    private fun updateUI(taskId: String, messageList: List<Messages>) {
        logger.log(TAG, "updateUI")
        sendReadingTime(taskId, messageList)
        _messagesList.value = SuccessRequest(messageList)
    }

    private fun sendReadingTime(taskId: String, messageList: List<Messages>) {
        viewModelScope.launch {
            val taskReadingTime = LocalDateTime.now()
            if (messageList.isNotEmpty()) {
                val lastMessageTime: LocalDateTime =
                    messageList.maxBy { it.dateTime }.dateTime.toLocalDateTime()
                logger.log(TAG, "lastMessageTime: $lastMessageTime")
                setTaskAndMessageReadingTime(
                    credentials = getCredentials(),
                    taskId = taskId,
                    lastMessageTime,
                    taskReadingTime
                ).collect { request ->
                    when (request) {
                        is SuccessRequest -> {
                            delay(1000)
                            _messagesList.value =
                                SuccessRequest(messageList.map { it.copy(unread = false) })
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}