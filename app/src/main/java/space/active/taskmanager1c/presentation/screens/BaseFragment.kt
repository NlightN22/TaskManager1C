package space.active.taskmanager1c.presentation.screens

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.BackendException
import space.active.taskmanager1c.coreutils.CantShowSnackBar
import space.active.taskmanager1c.coreutils.EmptyObject
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.databinding.BottomNavigationMenuBinding
import space.active.taskmanager1c.domain.models.ClickableTask
import space.active.taskmanager1c.domain.models.FragmentDeepLinks
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.domain.use_case.HandleDeepLink
import space.active.taskmanager1c.presentation.screens.mainactivity.MainViewModel
import space.active.taskmanager1c.presentation.utils.Toasts
import space.active.taskmanager1c.presentation.utils.navigateWithAnim
import javax.inject.Inject

private const val TAG = "BaseFragment"
const val LOGIN_SUCCESSFUL = "LOGIN_SUCCESSFUL"

@AndroidEntryPoint
abstract class BaseFragment(fragment: Int) : Fragment(fragment) {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var toasts: Toasts

    @Inject
    lateinit var exceptionHandler: ExceptionHandler

    @Inject
    lateinit var handleDeepLink: HandleDeepLink

    private var currentDestination: NavDestination? = null

    val baseMainVM by viewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // errorEvent observer
        baseMainVM.showExceptionDialogEvent.collectOnStart {
            logger.log(TAG, "showExceptionDialogEvent collectLatest ${it.sendToServerData}")
            showSendErrorDialog(it)
        }
        // SnackBar observer
        baseMainVM.showSaveSnack.collectOnStart {
            showSaveCancelSnackBar(it.text, it.duration, lifecycleScope) {
                baseMainVM.saveTask(SaveEvents.BreakSave)
            }
        }

        baseMainVM.exitEvent.collectOnStart {
            logger.log(TAG, "Logout from application $it")
            if (it) {
                requireActivity().finishAffinity()
            }
        }

        currentDestination = findNavController().currentDestination

        val isLoginFragment: Boolean = currentDestination?.id == R.id.loginFragment
        if (!isLoginFragment) {
            val login = getLoginState()
            saveLoginStateToPreviousBackStack(login)
            checkLoginState(login)
        }
        showNavigationLog()
        initBottomMenu(getBottomMenu())
    }

    private fun saveLoginStateToPreviousBackStack(login: Boolean) {
        if (login) {
            val previousStateHandle = findNavController().previousBackStackEntry?.savedStateHandle
            previousStateHandle?.set(LOGIN_SUCCESSFUL, true)
        }
    }

    abstract fun getBottomMenu() : BottomNavigationView?

    abstract fun successLogin()

    abstract fun navigateToLogin()

    private fun initBottomMenu(bottomMenu: BottomNavigationView?) {
        bottomMenu?.let { menu->
            clearBottomMenuItemIconTintList(menu)
            val bindMenu = BottomNavigationMenuBinding.bind(menu)
            bindMenu.versionTV.root.text = context?.packageName?.let {
                context?.packageManager?.getPackageInfo(
                    it, 0
                )?.versionName
            }
        }
    }

    fun getLoginState(): Boolean {
        val currentStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        val previousLogin =
            findNavController().previousBackStackEntry?.savedStateHandle?.get<Boolean>(
                LOGIN_SUCCESSFUL
            )
        logger.log(TAG, "getLoginState previous login state: $previousLogin")
        previousLogin?.let {
            currentStateHandle?.set(LOGIN_SUCCESSFUL, it)
        }
        val currentLogin = currentStateHandle?.get<Boolean>(LOGIN_SUCCESSFUL)
        logger.log(TAG, "getLoginState current login state: $currentLogin")
        return currentLogin ?: false
    }

    var textChangeJob: Job? = null
    fun TextInputEditText.changeListener(block: (String) -> Unit) {
        addTextChangedListener {
            textChangeJob?.cancel()
            textChangeJob = lifecycleScope.launchWhenStarted {
                delay(200)
                block(it?.toString() ?: "")
            }
        }
    }

    fun clearBottomMenuItemIconTintList(bottomMenu: BottomNavigationView) {
        bottomMenu.itemIconTintList = null
    }

    fun showOptionsMenu(anchorView: View): PopupMenu? {
        val optionsMenu = PopupMenu(this.context, anchorView)
        optionsMenu.inflate(R.menu.options_menu)
        optionsMenu.show()
        return optionsMenu
    }

    fun setOnOptionsMenuClickListener(
        optionsMenu: PopupMenu,
        callBack: (menuItem: MenuItem) -> Unit
    ) {
        optionsMenu.setOnMenuItemClickListener {
            callBack(it)
            return@setOnMenuItemClickListener true
        }
    }

    fun clearUserCredentialsAndExit() {
        baseMainVM.clearAndExit()
    }

    fun navigate(directions: NavDirections) {
        try {
            val backDest = findNavController().previousBackStackEntry?.destination
            logger.log(
                TAG,
                "Navigate to: $directions" +
                        "\ncurrent fragment: ${currentDestination?.displayName}" +
                        "\nprevious fragment: ${backDest?.displayName}"
            )
            findNavController().navigateWithAnim(directions)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun navigate(clickableTask: ClickableTask) {
        try {
            if (clickableTask.id.isNotBlank()) {
                val deepLink = FragmentDeepLinks.Detailed(clickableTask.id)
                handleDeepLink(findNavController(), deepLink)
            } else {
                throw EmptyObject ("ClickableTask.id")
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun onBackClick() {
        try {
            val destination = findNavController().currentBackStackEntry?.destination
            val backDest = findNavController().previousBackStackEntry?.destination
            logger.log(
                TAG,
                "Navigation back. Previous fragment:${backDest?.displayName}\ncurrent fragment: ${currentDestination?.displayName} "
            )
            destination?.let {
                if (it.id == currentDestination?.id) {
                    requireActivity().onBackPressed()
                } else {
                    logger.log("onBackClick", "")
                    findNavController().popBackStack()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun <T> StateFlow<T>.collectOnCreated(listener: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            this@collectOnCreated.collectLatest {
                listener(it)
            }
        }
    }

    fun <T> Flow<T>.collectOnStart(listener: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            this@collectOnStart.collectLatest {
                listener(it)
            }
        }
    }

    fun <T> StateFlow<T>.collectOnStart(listener: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            val job = this.coroutineContext.job
            this@collectOnStart.collectLatest {
                listener(it)
            }
        }
    }

    fun <T> SharedFlow<T>.collectOnStart(listener: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            this@collectOnStart.collectLatest {
                listener(it)
            }
        }
    }

    fun showSnackBar(collectableShared: SharedFlow<UiText>) = wrapSnackExceptions {
        collectableShared.collectOnStart {
            Snackbar.make(requireView(), it.getString(requireContext()), Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    fun showSnackBar(text: UiText) = wrapSnackExceptions {
        Snackbar.make(requireView(), text.getString(requireContext()), Snackbar.LENGTH_SHORT).show()
    }

    fun shimmerShow(shimmerView: ShimmerFrameLayout, mainView: View, visibility: Boolean) {
        if (visibility) {
            shimmerView.visibility = View.VISIBLE
            shimmerView.startShimmer()
            mainView.visibility = View.INVISIBLE
        } else {
            shimmerView.visibility = View.GONE
            shimmerView.stopShimmer()
            mainView.visibility = View.VISIBLE
        }
    }

    fun showSaveCancelSnackBar(
        text: String,
        timer: Int,
        coroutineScope: CoroutineScope,
        listener: View.OnClickListener,
    ) = wrapSnackExceptions {
        var duration = timer
        val snack = Snackbar.make(requireView(), text, Snackbar.LENGTH_INDEFINITE)
        coroutineScope.launch(SupervisorJob()) {
            snack.setActionTextColor(
                resources.getColor(
                    R.color.button_not_pressed,
                    resources.newTheme()
                )
            )
            snack.setAction(getString(R.string.snackbar_cancel_button, duration), listener)
            snack.show()
            while (duration > 0) {
                snack.setAction(getString(R.string.snackbar_cancel_button, duration), listener)
                delay(1000)
                duration -= 1
            }
            snack.dismiss()

        }
    }

    private fun checkLoginState(login: Boolean) {
        val currentFragment = findNavController().currentDestination?.id
        val loginFragment: Int = R.id.loginFragment
        if (!login) {
            logger.log(TAG, "Not login")
            if (currentFragment != loginFragment) {
                logger.log(TAG, "Navigate to login")
                navigateToLogin()
            }
        } else {
            logger.log(TAG, "Success login")
            successLogin()
        }
    }

    private fun showNavigationLog() {
        val backDestination = findNavController().previousBackStackEntry
        logger.log(TAG, "backDestination: ${backDestination}")
        val currentDestination = findNavController().currentDestination
        logger.log(TAG, "currentFragment: ${currentDestination}")
        val currentBackStackEntry = findNavController().currentBackStackEntry
        logger.log(TAG, "currentBackStackEntry: ${currentBackStackEntry}")
    }

    private fun wrapSnackExceptions(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            exceptionHandler(CantShowSnackBar())
        }
    }


    var currentDialog: AlertDialog? = null
    private fun showSendErrorDialog(backendException: BackendException) {
        if (currentDialog != null) {
            if (currentDialog!!.isShowing) return
        }
        logger.log(TAG, "showSendErrorDialog")
        currentDialog = AlertDialog.Builder(context)
            .setMessage(R.string.error_dialog_message)
            .setTitle(R.string.error_dialog_title)
            .setCancelable(true)
            .setPositiveButton(R.string.error_dialog_ok) { _, _ ->
                sendBackendException(backendException)
            }
            .setNegativeButton(R.string.error_dialog_cancel) { _, _ ->
                baseMainVM.skipBackendException(backendException)
            }
            .create()
        currentDialog!!.show()
    }

    private fun sendBackendException(backendException: BackendException) {
        val emailTo = arrayOf("admin@komponent-m.ru", "it@komponent-m.ru")
        val emailSubject = "TaskManager1C backend error report"
        logger.log(TAG, "sendBackendException ${backendException.sendToServerData}")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
//            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, emailTo)
            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
            putExtra(Intent.EXTRA_TEXT, " To server: ${backendException.sendToServerData.toString()}" +
                    "\nCode: ${backendException.errorCode}" +
                    "\nBody ${backendException.errorBody}")
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }

    fun shareUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$uri")
        }
        val shareIntent = Intent.createChooser(intent, null)
        startActivity(shareIntent)
    }
}