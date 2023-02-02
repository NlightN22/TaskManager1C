package space.active.taskmanager1c.presentation.screens.task_attachments

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.repository.SettingsRepository
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import javax.inject.Inject


private const val TAG = "AttachmentsViewModel"

@HiltViewModel
class AttachmentsViewModel @Inject constructor(
    settings: SettingsRepository,
    logger: Logger,
) : BaseViewModel(settings, logger) {

    private val _listItems = MutableStateFlow(emptyList<InternalStorageItem>())
    val listItems = _listItems.asStateFlow()

    fun collectStorageItems() {

    }

    fun clickItem(internalStorageItem: InternalStorageItem) {
        if (internalStorageItem.cached) {
            // open in default app
        } else {
            // load file from server
            // save info to DB
            // change list state
        }
    }

}