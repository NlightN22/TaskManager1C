package space.active.taskmanager1c.presentation.screens.task_attachments

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.cache_storage.CachedFilesRepository
import space.active.taskmanager1c.data.local.cache_storage.models.CachedFile
import space.active.taskmanager1c.domain.models.TaskTitleViewState
import space.active.taskmanager1c.domain.models.TaskTitleViewState.Companion.setTitleState
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.repository.TasksRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.GetCredentials
import space.active.taskmanager1c.domain.use_case.ShowToast
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject


private const val TAG = "AttachmentsViewModel"

@HiltViewModel
class AttachmentsViewModel @Inject constructor(
    settings: SettingsRepository,
    logger: Logger,
    private val exceptionHandler: ExceptionHandler,
    private val cachedFilesRepository: CachedFilesRepository,
    private val tasksRepository: TasksRepository,
    private val getCredentials: GetCredentials,
    private val showToast: ShowToast
) : BaseViewModel(settings, logger) {

    private val _taskTitleViewState = MutableStateFlow(TaskTitleViewState())
    val taskTitleViewState = _taskTitleViewState.asStateFlow()

    private val _openFileEvent = MutableSharedFlow<CachedFile>()
    val openFileEvent = _openFileEvent.asSharedFlow()

    private val _currentTaskId = MutableStateFlow("")

    private val _listItems = MutableStateFlow<Request<List<CachedFile>>>(PendingRequest())
    val listItems = _listItems.asStateFlow()

    fun collectStorageItems(taskId: String) {
        if (this.isResumed(taskId)) return
        _currentTaskId.value = taskId
        viewModelScope.launch {
            cachedFilesRepository.getFileList(
                getCredentials().toAuthBasicDto(),
                _currentTaskId.value
            )
                .catch {
                    exceptionHandler(it)
                    _listItems.value = ErrorRequest(it)
                }
                .collect { inputList ->
                    if (inputList != _listItems.value) {
                        _listItems.value = SuccessRequest(inputList)
                    }
                }
        }
        viewModelScope.launch {
            _taskTitleViewState.setTitleState(tasksRepository, taskId)
        }
    }

    private fun AttachmentsViewModel.isResumed(taskId: String): Boolean {
        return taskId == _currentTaskId.value
    }

    fun clickItem(cachedFile: CachedFile) {
        logger.log(TAG, "$cachedFile clicked")
        if (cachedFile.isLoading()) return
        if (cachedFile.cached) {
            // open in default app
            logger.log(TAG, "cached item clicked")
            if (!cachedFile.notUploaded) {
                // if uploaded - open in android app
                openCachedFile(cachedFile)
            } else if (cachedFile.notUploaded) {
                // if not uploaded and has error - start uploading
                uploadFileToServer(cachedFile)
            }
        } else {
            downloadFileFromServer(cachedFile)
            logger.log(TAG, "not cached item clicked")
        }
    }

    fun uploadFileToServer(cachedFile: CachedFile) {
        if (cachedFile.isLoading()) return
        viewModelScope.launch {
            cachedFilesRepository.uploadFileToServer(
                getCredentials().toAuthBasicDto(),
                cachedFile,
                _currentTaskId.value
            )
                .catch {
                    if (it is BackendException &&
                        it.errorCode == "500" &&
                        it.errorBody.contains("forbidden")
                    ) {
                        showToast(UiText.Resource(R.string.attachments_name_exist, cachedFile.filename))
                    } else {
                        exceptionHandler(it)
                    }
                }
                .collect { request ->
                    when (request) {
                        is SuccessRequest -> {
                            showToast(
                                UiText.Resource(
                                    R.string.attachments_end_loading,
                                    cachedFile.filename
                                )
                            )
                        }
                        is PendingRequest -> {
                        }
                        is ErrorRequest -> {
                            showToast(
                                UiText.Resource(
                                    R.string.attachments_error_loading,
                                    cachedFile.filename
                                )
                            )
                        }
                    }
                }
        }
    }

    fun openCachedFile(cachedFile: CachedFile) {
        if (cachedFile.isLoading()) return
        viewModelScope.launch {
            _openFileEvent.emit(cachedFile)
        }
    }

    fun deleteCachedFile(cachedFile: CachedFile) {
        if (cachedFile.isLoading()) return
        viewModelScope.launch {
            cachedFilesRepository.deleteCachedFile(cachedFile).collect {
                if (!it) showToast(UiText.Resource(R.string.attachments_delete_error))
            }
        }
    }

    fun deleteFileFromServer(cachedFile: CachedFile) {
        if (cachedFile.isLoading()) return
        viewModelScope.launch {
            cachedFilesRepository.deleteFileFromServer(
                getCredentials().toAuthBasicDto(),
                cachedFile,
                _currentTaskId.value
            )
                .catch { exceptionHandler(it) }
                .collect{
                    if (it) showToast(UiText.Resource(R.string.attachments_delete_from_server, cachedFile.filename))
                }
        }
    }

    fun downloadFileFromServer(cachedFile: CachedFile) {
        if (cachedFile.isLoading() || cachedFile.isDownloaded()) return
        viewModelScope.launch {
            cachedFilesRepository.downloadFromServerToCache(
                getCredentials().toAuthBasicDto(),
                cachedFile,
                _currentTaskId.value,
            )
                .catch { exceptionHandler(it) }
                .collect { request ->
                    when (request) {
                        is ProgressRequest -> {

                        }
                        is PendingRequest -> {}
                        is ErrorRequest -> {
                            showToast(
                                UiText.Resource(
                                    R.string.attachments_error_loading,
                                    cachedFile.filename
                                )
                            )
                        }
                        is SuccessRequest -> {
                            showToast(
                                UiText.Resource(
                                    R.string.attachments_end_loading,
                                    cachedFile.filename
                                )
                            )
                        }
                    }
                }
        }
    }

    fun startAutoUploadingAll() {
        viewModelScope.launch {
            _listItems.getSuccess()?.let { list ->
                list.filter { it.notUploaded }
                    .filterNot { it.loading }
                    .forEach {
                        logger.log(TAG, "startAutoUploading: $it")
                        uploadFileToServer(it)
                        delay(1000)
                    }
            }
        }
    }

    private fun <T> StateFlow<Request<T>>.getSuccess(): T? {
        when (this.value) {
            is SuccessRequest -> {
                val value = this.value as SuccessRequest
                return value.data
            }
            else -> {
                return null
            }
        }
    }

    private fun CachedFile.isLoading(): Boolean {
        val list = _listItems.value
        return list is SuccessRequest && list.data.find { it.id == this.id }?.loading ?: true
    }

    private fun CachedFile.isDownloaded(): Boolean {
        val list = _listItems.value
        return list is SuccessRequest && list.data.find { it.id == this.id }?.cached ?: true
    }

}