package space.active.taskmanager1c.domain.use_case

import android.util.Log
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.coreutils.nowDiffInDays
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.TaskUserIs
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.models.Validation
import java.time.LocalDateTime
import javax.inject.Inject

class Validate @Inject constructor() {

    fun title(title: String): Validation {
        val titleMaxLength = 120
        if (title.length < titleMaxLength && title.isNotEmpty()) {
            return Validation(true)
        }
        return Validation(
            false,
            UiText.Resource(
                R.string.title_valid_error,
                titleMaxLength
            )
        )
    }

    fun endDate(date: LocalDateTime?): Validation {
        val maxDateRange = 3 * 365 // 3 years
        val minDateRange = 0
        date?.let {
            if (date.nowDiffInDays() >= minDateRange && date.nowDiffInDays() <= maxDateRange) {
                return Validation(true)
            }
            if (date.nowDiffInDays() < minDateRange) {
                return Validation(false, UiText.Resource(R.string.end_date_valid_min_error))
            }
            if (date.nowDiffInDays() > maxDateRange) {
                return Validation(
                    false,
                    UiText.Resource(R.string.end_date_valid_max_error, maxDateRange)
                )
            }
        } ?: kotlin.run {
            return Validation(true)
        }
        return Validation(false, UiText.Resource(R.string.end_date_valid_error, maxDateRange))
    }

    fun author(author: User?): Validation {
        author?.let {
            if (it.name.isNotBlank() && it.id.isNotBlank()) {
                return Validation(true)
            }
        }
        return Validation(false, UiText.Resource(R.string.author_valid_error))
    }

    fun performer(performer: User?): Validation {
        Log.d("Validate", "performer $performer")
        performer?.let {
            if (it.name.isNotBlank() && it.id.isNotBlank()) {
                return Validation(true)
            }
        }
        return Validation(false, UiText.Resource(R.string.performer_valid_error))
    }

    fun okCancelChoose(ok: Boolean, userIs: TaskUserIs, newStatus: Task.Status): Validation {
        Log.d("Status validate", "userIs $userIs newStatus $newStatus")
        when (userIs) {
            is TaskUserIs.Author -> {
                // not in Reviewed and Finished changeType true can set Finished
                // not in Finished changeType false can set Performed
                if (ok) {
                    if (newStatus == Task.Status.Finished) {
                        return Validation(true)
                    }
                } else {
                    if (newStatus != Task.Status.Finished) {
                        return Validation(true)
                    }
                }
                return Validation(
                    false,
                    UiText.ResInRes(
                        R.string.author_status_valid_error, listOf(
                            Task.Status.Performed.getResId(),
                            Task.Status.Finished.getResId()
                        )
                    )
                )
            }
            is TaskUserIs.AuthorInReviewed -> {
                // author can set only Performed Finished in Reviewed
                if (newStatus == Task.Status.Performed || newStatus == Task.Status.Finished) {
                    return Validation(true)
                }
                return Validation(
                    false,
                    UiText.ResInRes(
                        R.string.author_status_valid_error, listOf(
                            Task.Status.Performed.getResId(),
                            Task.Status.Finished.getResId()
                        )
                    )
                )
            }
            is TaskUserIs.AuthorInNewTask -> {
                return Validation(true)
            }
            is TaskUserIs.Performer -> {
                // performer can set only Reviewed
                if (newStatus == Task.Status.Reviewed) {
                    return Validation(true)
                }
                return Validation(
                    false,
                    UiText.ResInRes(
                        R.string.performer_status_valid_error,
                        listOf(Task.Status.Reviewed.getResId())
                    )
                )
            }

            is TaskUserIs.PerformerInReviewed -> {
                // Can set only Accepted
                if (newStatus == Task.Status.Accepted) {
                    return Validation(true)
                }
                return Validation(
                    false,
                    UiText.ResInRes(
                        R.string.performer_status_valid_error,
                        listOf(Task.Status.Accepted.getResId())
                    )
                )
            }
            is TaskUserIs.Observer -> {
                // nothing to change
                return Validation(false, UiText.Resource(R.string.status_valid_error))
            }
            is TaskUserIs.NotAuthorOrPerformer -> {
                return Validation(false, UiText.Resource(R.string.status_valid_error))
            }
        }
    }

}