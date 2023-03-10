package space.active.taskmanager1c.domain.models

import android.content.UriMatcher
import android.net.Uri
import android.os.Bundle
import android.util.Log
import space.active.taskmanager1c.R
import space.active.taskmanager1c.presentation.screens.messages.MessagesFragmentArgs
import space.active.taskmanager1c.presentation.screens.task_attachments.AttachmentsFragmentArgs
import space.active.taskmanager1c.presentation.screens.task_detailed.TaskDetailedFragmentArgs

sealed class FragmentDeepLinks {
    abstract val taskId: String
    abstract val destination: Int
    abstract val bundleArg: Bundle

    data class Messages(
        override val taskId: String,
        override val destination: Int = R.id.messagesFragment,
        override val bundleArg: Bundle = MessagesFragmentArgs(taskId).toBundle()
    ) : FragmentDeepLinks() {
        companion object {
            const val matcherId = 1
            const val matcherPath = "taskmgr/hs/taskmgr/tasks/messages"
        }
    }

    data class Attachments(
        override val taskId: String,
        override val destination: Int = R.id.attachmentsFragment,
        override val bundleArg: Bundle = AttachmentsFragmentArgs(taskId).toBundle()
    ) : FragmentDeepLinks() {
        companion object {
            const val matcherId = 2
            const val matcherPath = "taskmgr/hs/taskmgr/tasks/*/file"
        }
    }

    data class Detailed(
        override val taskId: String,
        override val bundleArg: Bundle = TaskDetailedFragmentArgs(taskId).toBundle(),
        override val destination: Int = R.id.taskDetailedFragment
    ) : FragmentDeepLinks() {
        companion object {
            const val matcherId = 3
            const val matcherPath = "taskmgr/hs/taskmgr/tasks/*"
        }
    }

    fun isCurrentLink(currentFragmentId: Int?, taskId: String?): Boolean {
        return currentFragmentId == this.destination && taskId == this.taskId
    }

    companion object {
        private val matcher = MyUriMatcher()
        fun getFragmentLink(uri: Uri): FragmentDeepLinks? {
            val matchedUri = matcher.match(uri)
            Log.d("FragmentDeepLinks", "matchedUri: $matchedUri")
            return when (matchedUri) {
                Messages.matcherId -> {
                    val taskId = uri.getQueryParameter("id")
                    taskId?.let {
                        Messages(it)
                    }
                }
                Attachments.matcherId -> {
                    try {
                        val taskId = uri.pathSegments[4]
                        taskId?.let {
                            return Attachments(it)
                        }
                    } catch (e: Exception) {
                        return null
                    }
                }
                Detailed.matcherId -> {
                    // get query
                    val taskId = uri.lastPathSegment
                    taskId?.let {
                        return Detailed(it)
                    }
                }
                else -> null
            }
        }
    }

    private class MyUriMatcher() : UriMatcher(NO_MATCH) {
        private val authority = "taskmgr.komponent-m.ru"

        init {
            addURI(
                authority,
                Messages.matcherPath,
                Messages.matcherId
            ) // parse messages?id=4ce6cb44-a3c4-11ea-8d5a-00155d28010b
            addURI(
                authority,
                Attachments.matcherPath,
                Attachments.matcherId
            )
            addURI(
                authority,
                Detailed.matcherPath,
                Detailed.matcherId
            )
        }
    }
}




