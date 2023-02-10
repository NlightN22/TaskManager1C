package space.active.taskmanager1c.domain.models

import android.net.Uri

data class InternalStorageFile(
    val uri: Uri?,
    val id: String?,
    val filename: String,
    val cached: Boolean = false,
    val loading: Boolean = false,
    val notUploaded: Boolean = false
)