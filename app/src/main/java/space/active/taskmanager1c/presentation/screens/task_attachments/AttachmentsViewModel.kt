package space.active.taskmanager1c.presentation.screens.task_attachments

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.cache_storage.CachedFilesRepository
import space.active.taskmanager1c.data.local.cache_storage.models.CachedFile
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.GetCredentials
import space.active.taskmanager1c.domain.use_case.ShowErrorToast
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject


private const val TAG = "AttachmentsViewModel"

@HiltViewModel
class AttachmentsViewModel @Inject constructor(
    settings: SettingsRepository,
    logger: Logger,
    private val exceptionHandler: ExceptionHandler,
    private val cachedFilesRepository: CachedFilesRepository,
    private val getCredentials: GetCredentials,
    private val showErrorToast: ShowErrorToast
) : BaseViewModel(settings, logger) {

    private val _openFileEvent = MutableSharedFlow<CachedFile>()
    val openFileEvent = _openFileEvent.asSharedFlow()

    private val _currentTaskId = MutableStateFlow("")

    private val _listItems = MutableStateFlow(emptyList<CachedFile>())
    val listItems = _listItems.asStateFlow()

    fun collectStorageItems(taskId: String) {
        _currentTaskId.value = taskId
        viewModelScope.launch {
            cachedFilesRepository.getFileList(
                getCredentials().toAuthBasicDto(),
                _currentTaskId.value
            )
                .catch { exceptionHandler(it) }
                .collect { inputList ->
                    if (inputList != _listItems.value) {
//                        logger.log(TAG, "inputList:\n${inputList.joinToString("\n")}")
                        _listItems.value = inputList
                    }
                }
        }
    }

    fun clickItem(cachedFile: CachedFile) {
        logger.log(TAG, "$cachedFile clicked")
        if (cachedFile.loading) return
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
        if (cachedFile.loading) return
        viewModelScope.launch {
            cachedFilesRepository.uploadFileToServer(
                getCredentials().toAuthBasicDto(),
                cachedFile,
                _currentTaskId.value
            )
                .catch {
                    if (it is BackendException &&
                        it.errorCode == "500" &&
                        it.errorBody.contains("Уже есть файл с таким наименованием")) {
                        showErrorToast(it)
                    } else {
                        exceptionHandler(it)
                    }
                }
                .collect { request ->
                    when (request) {
                        is SuccessRequest -> {
                            showErrorToast(
                                UiText.Resource(
                                    R.string.attachments_end_loading,
                                    cachedFile.filename
                                )
                            )
                        }
                        is PendingRequest -> {
//                            showErrorToast(
//                                UiText.Resource(
//                                    R.string.attachments_start_loading,
//                                    cachedFile.filename
//                                )
//                            )
                        }
                        is ErrorRequest -> {
                            showErrorToast(
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
        if (cachedFile.loading) return
        viewModelScope.launch {
            _openFileEvent.emit(cachedFile)
        }
    }

    fun deleteCachedFile(cachedFile: CachedFile) {
        if (cachedFile.loading) return
        viewModelScope.launch {
            cachedFilesRepository.deleteCachedFile(cachedFile).collect {
                if (!it) showErrorToast(UiText.Resource(R.string.attachments_delete_error))
            }
        }
    }

    fun downloadFileFromServer(cachedFile: CachedFile) {
        if (cachedFile.loading) return
        viewModelScope.launch {
            cachedFilesRepository.downloadFromServerToCache(
                getCredentials().toAuthBasicDto(),
                cachedFile,
                _currentTaskId.value,
            )
                .catch { exceptionHandler(it) }
                .collect { request ->
                    when (request) {
                        is PendingRequest -> {
//                            showErrorToast(
//                                UiText.Resource(
//                                    R.string.attachments_start_loading,
//                                    cachedFile.filename
//                                )
//                            )
                        }
                        is ErrorRequest -> {
                            showErrorToast(
                                UiText.Resource(
                                    R.string.attachments_error_loading,
                                    cachedFile.filename
                                )
                            )
                        }
                        is SuccessRequest -> {
                            showErrorToast(
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
//        logger.log(TAG, "startAutoUploadingAll:\n${_listItems.value.joinToString("\n")}")
//        if (cachedFile.notUploaded) {
//            viewModelScope.launch {
//                logger.log(TAG, "startAutoUploading: $cachedFile")
//                uploadFileToServer(cachedFile)
//            }
//        }
        viewModelScope.launch {
            _listItems.value
                .filter { it.notUploaded }
                .filterNot { it.loading }
                .forEach {
                    logger.log(TAG, "startAutoUploading: $it")
                    uploadFileToServer(it)
                    delay(1000)
                }
        }
    }


}