package space.active.taskmanager1c.presentation.screens

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.CantShowSnackBar
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.presentation.screens.mainactivity.MainViewModel
import space.active.taskmanager1c.presentation.utils.Toasts
import java.util.Date
import javax.inject.Inject

private const val TAG = "BaseFragment"

abstract class BaseFragment(fragment: Int) : Fragment(fragment) {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var toasts: Toasts

    @Inject
    lateinit var exceptionHandler: ExceptionHandler

    private val baseMainVM by viewModels<MainViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // SnackBar observer
        lifecycleScope.launchWhenStarted {
            baseMainVM.showSaveSnack.collectLatest {
                showSaveCancelSnackBar(it.text, it.duration, lifecycleScope) {
                    baseMainVM.saveTask(SaveEvents.BreakSave)
                }
            }
        }
    }

    fun clearBottomMenuItemIconTintList(bottomMenu: BottomNavigationView) {
        bottomMenu.itemIconTintList = null
    }

    fun showOptionsMenu(context: Context?, anchorView: View): PopupMenu? {
        context?.let { context ->
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

    fun launchSettings(action: Int) {
        findNavController().navigate(action)
    }

    fun onBackClick() {
        val destination = findNavController().currentBackStackEntry?.destination?.id
        destination?.let {
            if (it == findNavController().currentDestination?.id) {
                requireActivity().onBackPressed()
            } else {
                findNavController().popBackStack()
            }
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

    fun showSnackBar(collectableShared: SharedFlow<String>) {
        collectableShared.collectOnStart {
            Snackbar.make(requireView(), it, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun showSnackBar(text: String) {
        Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT).show()
    }

    fun shimmerShow(shimmerView: ShimmerFrameLayout, recyclerView: RecyclerView, visibility: Boolean) {
        if (visibility) {
            shimmerView.visibility = View.VISIBLE
            shimmerView.startShimmer()
            recyclerView.visibility = View.GONE
        } else {
            shimmerView.visibility = View.GONE
            shimmerView.stopShimmer()
            recyclerView.visibility = View.VISIBLE
        }
    }

    fun showSaveCancelSnackBar(
        text: String,
        timer: Int,
        coroutineScope: CoroutineScope,
        listener: View.OnClickListener,
    ) {
        var duration = timer
        try {
            val snack = Snackbar.make(requireView(), text, Snackbar.LENGTH_INDEFINITE)
            coroutineScope.launch(SupervisorJob()) {
                snack.setActionTextColor(resources.getColor(R.color.button_not_pressed))
                snack.setAction(getString(R.string.snackbar_cancel_button, duration), listener)
                snack.show()
                while (duration > 0) {
                    snack.setAction(getString(R.string.snackbar_cancel_button, duration), listener)
                    delay(1000)
                    duration -= 1
                }
                snack.dismiss()

            }
        } catch (e: IllegalArgumentException) {
            exceptionHandler(CantShowSnackBar(e.message ?: ""))
        }
    }
}