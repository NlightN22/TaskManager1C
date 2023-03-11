package space.active.taskmanager1c.presentation.utils.dialogs

import android.os.Parcelable
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class EditTextDialogStates(
    @StringRes
    val hint: Int? = null ,
    val text: String? = null,
    @IntegerRes
    val maxLength: Int? = null,
    val ok: Boolean = true,
    val cancel: Boolean = true,
) : Parcelable
