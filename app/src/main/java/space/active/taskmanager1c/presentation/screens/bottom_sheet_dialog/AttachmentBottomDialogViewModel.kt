package space.active.taskmanager1c.presentation.screens.bottom_sheet_dialog

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.local.cache_storage.CachedFilesRepository
import space.active.taskmanager1c.data.local.cache_storage.CachedFilesRepositoryImpl
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.ShowToast
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AttachmentBottomDialogViewModel @Inject constructor(
    settings: SettingsRepository,
    logger: Logger,
    private val cachedFilesRepository: CachedFilesRepository,
    private val exceptionHandler: ExceptionHandler,
    private val showToast: ShowToast
) : BaseViewModel(settings, logger) {

    private val _saveNewPhotoEvent = MutableSharedFlow<Uri>()
    val saveNewPhotoEvent = _saveNewPhotoEvent.asSharedFlow()
    private val _selectNewPhotoEvent = MutableSharedFlow<String>()
    val selectNewPhotoEvent = _selectNewPhotoEvent.asSharedFlow()
    private val _selectNewFileEvent = MutableSharedFlow<String>()
    val selectNewFileEvent = _selectNewFileEvent.asSharedFlow()
    private val _saveFinishedEvent = MutableSharedFlow<String>()
    val saveFinishedEvent = _saveFinishedEvent.asSharedFlow()

    private lateinit var taskId: String

    fun initArgs(inputTaskId: String) {
        taskId = inputTaskId
    }

    fun clickNewPhoto() {
        viewModelScope.launch {
            val newContentUri = cachedFilesRepository.getCacheUriForSave(taskId)
            _saveNewPhotoEvent.emit(newContentUri)
        }
    }

    fun clickSelectNewPhoto() {
        viewModelScope.launch {
            _selectNewPhotoEvent.emit("image/*")
        }
    }

    fun clickSelectNewFile() {
        viewModelScope.launch {
            _selectNewFileEvent.emit("*/*")
        }
    }

    fun finishSave(result: String) {
        viewModelScope.launch {
            _saveFinishedEvent.emit(result)
        }
    }

    fun saveSelectedExternalFile(uri: Uri) {
        viewModelScope.launch {
            cachedFilesRepository.saveExternalFileToCache(uri, cacheDirPath = taskId)
                .catch { e ->
                    when (e) {
                        is CachedFilesRepositoryImpl.SizeTooHighException -> {
                            showToast(
                                UiText.Resource(
                                    R.string.attachments_too_large,
                                    e.name,
                                    (CachedFilesRepositoryImpl.MAX_FILE_SIZE_BYTES / 1024 / 1024).toString()
                                )
                            )
                        }

                        is CachedFilesRepositoryImpl.DuplicateFileException -> {
                            showToast(
                                UiText.Resource(
                                    R.string.attachments_file_exist,
                                    e.name
                                )
                            )
                        }

                        else -> exceptionHandler(e)
                    }
                }
                .collectInScope { isSuccess ->
                    if (isSuccess) {
                        finishSave(result = "File saved successfully")
                    } else {
                        showToast(UiText.Resource(R.string.bottom_sheet_error_save_file))
                    }
                }
        }
    }
}