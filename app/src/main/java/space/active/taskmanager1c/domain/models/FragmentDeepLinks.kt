package space.active.taskmanager1c.domain.models

import android.content.UriMatcher
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.net.toUri
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.di.BASE_URL
import space.active.taskmanager1c.di.START_PATH
import space.active.taskmanager1c.presentation.screens.messages.MessagesFragmentArgs
import space.active.taskmanager1c.presentation.screens.task_attachments.AttachmentsFragmentArgs
import space.active.taskmanager1c.presentation.screens.task_detailed.TaskDetailedFragmentArgs

/**
 * @param matcherId must contains unique id for matcher
 * @param matcherPath must contains path in UriMatcher path format.
 * "*" may be used as a wild card for any text, and "#" may be used as a wild card for numbers.
 */
sealed class FragmentDeepLinks {
    abstract val taskId: String
    abstract val destination: Int
    abstract val bundleArg: Bundle
    abstract fun toUri(): Uri

    data class Messages(
        override val taskId: String,
        override val destination: Int = R.id.messagesFragment,
        override val bundleArg: Bundle = MessagesFragmentArgs(taskId).toBundle()
    ) : FragmentDeepLinks() {

        override fun toUri(): Uri {
            val startPath = getFullStartPath()
            val combinedPath = startPath + uriPath + taskId
            return combinedPath.toUri()
        }

        companion object {
            const val matcherId = 1
            const val matcherPath = START_PATH + "tasks/messages"
            const val uriPath = START_PATH + "tasks/messages?id="
        }
    }

    data class Attachments(
        override val taskId: String,
        override val destination: Int = R.id.attachmentsFragment,
        override val bundleArg: Bundle = AttachmentsFragmentArgs(taskId).toBundle()
    ) : FragmentDeepLinks() {

        override fun toUri(): Uri {
            val startPath = getFullStartPath()
            val replacedMatcher = matcherPath.replace("*", taskId)
            val combinedPath = startPath + replacedMatcher
            return combinedPath.toUri()
        }

        companion object {
            const val matcherId = 2
            const val matcherPath = START_PATH + "tasks/*/file"
        }
    }

    data class Detailed(
        override val taskId: String,
        override val bundleArg: Bundle = TaskDetailedFragmentArgs(taskId).toBundle(),
        override val destination: Int = R.id.taskDetailedFragment,
    ) : FragmentDeepLinks() {

        override fun toUri(): Uri {
            val startPath = getFullStartPath()
            val replacedMatcher = matcherPath.replace("*", taskId)
            val combinedPath = startPath + replacedMatcher
            return combinedPath.toUri()
        }

        companion object {
            const val matcherId = 3
            const val matcherPath = START_PATH + "tasks/*"
        }
    }

    fun isCurrentLink(currentFragmentId: Int?, taskId: String?): Boolean {
        return currentFragmentId == this.destination && taskId == this.taskId
    }

    fun getSchemeAndHost(): String {
        val uri = BASE_URL.toUri()
        val scheme = uri.scheme ?: throw EmptyObject("getSchemeAndHost scheme")
        val host = uri.host ?: throw EmptyObject("getSchemeAndHost host")
        return "$scheme://$host/"
    }

    fun getFullStartPath(): String {
        val schemeAndHost = getSchemeAndHost()
        val fullPath = schemeAndHost + START_PATH
        return fullPath
    }


    companion object {
        private val authority = BASE_URL.toUri().host ?: throw EmptyObject("MyUriMatcher authority")
        private val matcher = MyUriMatcher(authority)
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

    private class MyUriMatcher(authority: String) : UriMatcher(NO_MATCH) {
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