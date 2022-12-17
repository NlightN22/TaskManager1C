package space.active.taskmanager1c.presentation.screens

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.presentation.screens.mainactivity.MainViewModel
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.presentation.utils.Toasts
import javax.inject.Inject

private const val TAG = "BaseFragment"

abstract class BaseFragment(fragment: Int) : Fragment(fragment) {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var toasts: Toasts

    private val baseMainVM by viewModels<MainViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // SnackBar observer
        lifecycleScope.launchWhenStarted {
            baseMainVM.showSaveSnack.collectLatest {
                showSaveCancelSnackBar(it.text, view, it.duration, lifecycleScope) {
                    baseMainVM .saveTask(SaveEvents.BreakSave)
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

    fun showSnackBar(text: String, view: View) {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show()
    }

    fun showSaveCancelSnackBar(
        text: String,
        view: View,
        timer: Int,
        coroutineScope: CoroutineScope,
        listener: View.OnClickListener,
    ) {
        var duration = timer
        val snack = Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE)
        coroutineScope.launch(SupervisorJob()) {
//            try {
                snack.setActionTextColor(resources.getColor(R.color.button_not_pressed))
                snack.setAction(getString(R.string.snackbar_cancel_button, duration), listener)
                snack.show()
                while (duration > 0) {
                    snack.setAction(getString(R.string.snackbar_cancel_button, duration), listener)
                    delay(1000)
                    duration -= 1
                }
                snack.dismiss()
//            } catch (e: CancellationException) {
//                snack.dismiss()
//            }
        }
    }

}