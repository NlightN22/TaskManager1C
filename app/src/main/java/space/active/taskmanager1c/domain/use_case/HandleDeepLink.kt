package space.active.taskmanager1c.domain.use_case

import android.content.Intent
import android.os.Bundle
import androidx.navigation.NavController
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.FragmentDeepLinks
import space.active.taskmanager1c.presentation.screens.messages.MessagesFragmentArgs
import space.active.taskmanager1c.presentation.screens.task_attachments.AttachmentsFragmentArgs
import space.active.taskmanager1c.presentation.screens.task_detailed.TaskDetailedFragmentArgs
import space.active.taskmanager1c.presentation.utils.Toasts
import space.active.taskmanager1c.presentation.utils.navigateWithAnim
import javax.inject.Inject

private const val TAG = "HandleDeepLink"

class HandleDeepLink @Inject constructor(
    private val logger: Logger,
    private val toasts: Toasts
){
    operator fun invoke(navController: NavController, intent: Intent) {
        val intentUri = intent.data
        intentUri?.let { uri ->
            logger.log(TAG, "handle uri: $uri")
            val fragmentDL = FragmentDeepLinks.getFragmentLink(uri)
            fragmentDL?.navigate(navController)
                ?: run {
                logger.log(TAG, "Link not found ${fragmentDL}")
                toasts(UiText.Resource(R.string.wrong_link, uri.toString()))
            }
        }
    }

    operator fun invoke(navController: NavController, fragmentDeepLinks: FragmentDeepLinks) {
        fragmentDeepLinks.navigate(navController)
    }

    private fun FragmentDeepLinks.navigate(navController: NavController) {
        when (this) {
            is FragmentDeepLinks.Detailed -> {
                logger.log(TAG, "handleDeepLink open Detailed ${this}")
                val currentFragmentId = navController.currentDestination?.id
                val taskId: String? = getCurrentArgs(navController) {
                    TaskDetailedFragmentArgs.fromBundle(it).taskId
                }
                logger.log(TAG, "currentFragmentId ${currentFragmentId} taskId $taskId")
                if (!this.isCurrentLink(currentFragmentId, taskId)) {
                    navController.navigateWithAnim(this.destination, args = this.bundleArg)
                } else {
                    toasts(UiText.Resource(R.string.link_already_opened))
                }
            }
            is FragmentDeepLinks.Messages -> {
                logger.log(TAG, "handleDeepLink open Messages ${this}")
                val currentFragmentId = navController.currentDestination?.id
                val taskId: String? = getCurrentArgs(navController) {
                    MessagesFragmentArgs.fromBundle(it).taskId
                }
                if (!this.isCurrentLink(currentFragmentId, taskId)) {
                    navController.navigateWithAnim(this.destination, args = this.bundleArg)
                } else {
                    toasts(UiText.Resource(R.string.link_already_opened))
                }
            }
            is FragmentDeepLinks.Attachments -> {
                logger.log(TAG, "handleDeepLink open Attachments ${this}")
                val currentFragmentId = navController.currentDestination?.id
                val taskId: String? = getCurrentArgs(navController) {
                    AttachmentsFragmentArgs.fromBundle(it).taskId
                }
                if (!this.isCurrentLink(currentFragmentId, taskId)) {
                    navController.navigateWithAnim(this.destination, args = this.bundleArg)
                } else {
                    toasts(UiText.Resource(R.string.link_already_opened))
                }
            }
        }
    }

    private fun <T> getCurrentArgs(navController: NavController, block: (bundle:Bundle) -> T): T? {
        val currentArgs = navController.currentBackStackEntry?.arguments
        return currentArgs?.let {
            try {
                block(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}