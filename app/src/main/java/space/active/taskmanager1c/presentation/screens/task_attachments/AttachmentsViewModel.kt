package space.active.taskmanager1c.presentation.screens.task_attachments

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.InternalStorageFile
import space.active.taskmanager1c.domain.repository.FilesRepository
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.GetCredentials
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject


private const val TAG = "AttachmentsViewModel"

@HiltViewModel
class AttachmentsViewModel @Inject constructor(
    settings: SettingsRepository,
    logger: Logger,
    private val exceptionHandler: ExceptionHandler,
    private val filesRepository: FilesRepository,
    private val getCredentials: GetCredentials,
) : BaseViewModel(settings, logger) {

    private val _listItems = MutableStateFlow(emptyList<InternalStorageFile>())
    val listItems = _listItems.asStateFlow()

    private val _openFileEvent = MutableSharedFlow<InternalStorageFile>()
    val openFileEvent = _openFileEvent.asSharedFlow()

    private val _deleteFileEvent = MutableSharedFlow<InternalStorageFile>()
    val deleteFileEvent = _deleteFileEvent.asSharedFlow()

    private val _currentTaskId = MutableStateFlow("")

    fun collectStorageItems(taskId: String) {
        _currentTaskId.value = taskId
        viewModelScope.launch {
            filesRepository.getFileList(getCredentials().toAuthBasicDto(), _currentTaskId.value)
                .catch { exceptionHandler(it) }
                .collect {
                    _listItems.value = it
                }
        }
    }

    fun clickItem(internalStorageFile: InternalStorageFile) {
        logger.log(TAG, "$internalStorageFile clicked")
        if (internalStorageFile.cached) {
            // open in default app
            logger.log(TAG, "cached item clicked")
            if (!internalStorageFile.notUploaded) {
                // if uploaded - open in android app
                openCachedFile(internalStorageFile)
            } else if (internalStorageFile.notUploaded && !internalStorageFile.loading) {
                // if not uploaded and has error - start uploading
                uploadFileToServer(internalStorageFile)
            }
        } else {
            downloadFileFromServer(internalStorageFile)
            logger.log(TAG, "not cached item clicked")
        }
    }

    fun uploadFileToServer(internalStorageFile: InternalStorageFile) {
        logger.log(TAG, "not implemented uploading")
    }

    fun openCachedFile(internalStorageFile: InternalStorageFile) {
        viewModelScope.launch {
            _openFileEvent.emit(internalStorageFile)
        }
    }

    fun deleteCachedFile(internalStorageFile: InternalStorageFile){
        viewModelScope.launch {
            _deleteFileEvent.emit(internalStorageFile)
        }
    }

    fun downloadFileFromServer(internalStorageFile: InternalStorageFile) {
        viewModelScope.launch {
            internalStorageFile.id?.let { fileId ->
                filesRepository.downloadFileToCache(
                    getCredentials().toAuthBasicDto(),
                    _currentTaskId.value,
                    fileId,
                    internalStorageFile.filename
                )
                    .catch { exceptionHandler(it) }
                    .collect { request ->
                        when (request) {
                            is PendingRequest -> {
                                logger.log(TAG, "Start download ${internalStorageFile.filename}")
                                // todo update list item
//                                _listItems.value = _listItems.value.map {
//                                    if (it.id == fileId) {
//                                        it.copy(loading = true)
//                                    } else {
//                                        it
//                                    }
//                                }
                            }
                            is ErrorRequest -> {
                                logger.log(TAG, "Error download ${internalStorageFile.filename}")
                            }
                            is SuccessRequest -> {
                                logger.log(TAG, "Finish download ${internalStorageFile.filename}")
                            }
                        }
                    }
            }
        }
    }

}