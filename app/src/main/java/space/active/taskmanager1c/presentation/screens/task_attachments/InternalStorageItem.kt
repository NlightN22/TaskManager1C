package space.active.taskmanager1c.presentation.screens.task_attachments

import android.net.Uri

data class InternalStorageItem(
    val uri: Uri?,
    val id: String,
    val filename: String,
    val cached: Boolean = false,
    val loading: Boolean = false,
)