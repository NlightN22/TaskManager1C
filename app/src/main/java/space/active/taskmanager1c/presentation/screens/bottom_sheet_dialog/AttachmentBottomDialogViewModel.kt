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
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.ShowErrorToast
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AttachmentBottomDialogViewModel @Inject constructor(
    settings: SettingsRepository,
    logger: Logger,
    private val cachedFilesRepository: CachedFilesRepository,
    private val exceptionHandler: ExceptionHandler,
    private val showErrorToast: ShowErrorToast
) : BaseViewModel(settings, logger) {

    private val _saveNewPhotoEvent = MutableSharedFlow<Uri>()
    val saveNewPhotoEvent = _saveNewPhotoEvent.asSharedFlow()
    private val _selectNewPhotoEvent = MutableSharedFlow<Boolean>()
    val selectNewPhotoEvent = _selectNewPhotoEvent.asSharedFlow()
    private val _selectNewFileEvent = MutableSharedFlow<String>() // MimeType string
    val selectNewFileEvent = _selectNewFileEvent.asSharedFlow()
    private val _saveFinishedEvent = MutableSharedFlow<String>()
    val saveFinishedEvent = _saveFinishedEvent.asSharedFlow()

    private lateinit var taskId: String

    fun initArgs(inputTaskId: String) {
        taskId = inputTaskId
    }

    fun clickNewPhoto() {
        viewModelScope.launch {
            // get uri from cached file repository to save new photo
            val newContentUri = cachedFilesRepository.getCacheUriForSave(taskId)
            // uri must be temp from content resolver
            _saveNewPhotoEvent.emit(newContentUri) // emit uri where to save
        }
    }

    fun clickSelectNewPhoto() {
        viewModelScope.launch {
            _selectNewPhotoEvent.emit(true)
        }
    }

    fun clickSelectNewFile() {
        viewModelScope.launch {
            _selectNewFileEvent.emit("*/*") // all file types
        }
    }

    fun finishSave(result: String) {
        viewModelScope.launch {
            _saveFinishedEvent.emit(result)
        }
    }

    fun saveSelectedExternalFile(uri: Uri) {
        cachedFilesRepository.saveExternalFileToCache(uri, taskId)
            .catch { exceptionHandler(it) }
            .collectInScope { isSuccess ->
            if (isSuccess) {
                finishSave("File saved successfully")
            } else {
                showErrorToast(UiText.Resource(R.string.bottom_sheet_error_save_file))
            }
        }
    }
}