package space.active.taskmanager1c.presentation.screens

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.CantShowSnackBar
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.presentation.screens.mainactivity.MainViewModel
import space.active.taskmanager1c.presentation.utils.Toasts
import javax.inject.Inject

private const val TAG = "BaseFragment"
const val LOGIN_SUCCESSFUL = "LOGIN_SUCCESSFUL"

abstract class BaseFragment(fragment: Int) : Fragment(fragment) {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var toasts: Toasts

    @Inject
    lateinit var exceptionHandler: ExceptionHandler

    private var currentDestination: NavDestination? = null

    val baseMainVM by viewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            checkLoginState(login)
        }
        showNavigationLog()
    }

    private fun getLoginState(): Boolean {
        val currentStateHandle = findNavController().currentBackStackEntry?.savedStateHandle

        val previousLogin =
            findNavController().previousBackStackEntry?.savedStateHandle?.get<Boolean>(
                LOGIN_SUCCESSFUL
            )
        previousLogin?.let {
            currentStateHandle?.set(LOGIN_SUCCESSFUL, it)
        }
        val currentLogin = currentStateHandle?.get<Boolean>(LOGIN_SUCCESSFUL)
        return currentLogin ?: false
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
        logger.log(TAG, "backDestination: ${backDestination?.destination}")
        val currentDestination = findNavController().currentDestination
        logger.log(TAG, "currentFragment: ${currentDestination?.displayName}")
        val currentBackStackEntry = findNavController().currentBackStackEntry
        logger.log(TAG, "currentBackStackEntry: ${currentBackStackEntry?.destination}")
    }

    abstract fun successLogin()

    abstract fun navigateToLogin()

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

    fun showOptionsMenu(context: Context?, anchorView: View): PopupMenu? {
        context?.let {
            val optionsMenu = PopupMenu(this.context, anchorView)
            optionsMenu.inflate(R.menu.options_menu)
            optionsMenu.show()
            return optionsMenu
        }
        return null
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
                "Nav: $directions  cur: ${currentDestination?.displayName} backdest: ${backDest?.displayName}"
            )
            findNavController().navigate(directions)
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
                "Nav back: ${backDest?.displayName} cur: ${currentDestination?.displayName} "
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

    fun <T> StateFlow<T>.collectOnCreate(listener: (T) -> Unit) {
        lifecycleScope.launchWhenCreated {
            this@collectOnCreate.collectLatest {
                listener(it)
            }
        }
    }

    fun <T> Flow<T>.collectOnStart(listener: (T) -> Unit) {
        lifecycleScope.launchWhenStarted {
            this@collectOnStart.collectLatest {
                listener(it)
            }
        }
    }

    fun <T> StateFlow<T>.collectOnStart(listener: (T) -> Unit) {
        lifecycleScope.launchWhenStarted {
            this@collectOnStart.collectLatest {
                listener(it)
            }
        }
    }

    fun <T> SharedFlow<T>.collectOnStart(listener: (T) -> Unit) {
        lifecycleScope.launchWhenStarted {
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

    private fun wrapSnackExceptions(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            exceptionHandler(CantShowSnackBar())
        }
    }
}