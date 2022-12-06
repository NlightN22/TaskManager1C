package space.active.taskmanager1c.presentation.screens

import android.content.Context
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.active.taskmanager1c.R

abstract class BaseFragment(fragment: Int) : Fragment(fragment) {


    fun clearBottomMenuItemIconTintList(bottomMenu: BottomNavigationView) {
        bottomMenu.itemIconTintList = null
    }

    fun showOptionsMenu(context: Context?, anchorView: View): PopupMenu? {
        context?.let { context ->
            val optionsMenu = PopupMenu(context, anchorView)
            optionsMenu.inflate(R.menu.options_menu)
            optionsMenu.show()
            return optionsMenu
        }
        return null
    }

    fun setOnOptionsMenuClickListener(optionsMenu: PopupMenu, callBack: (menuItem: MenuItem) -> Unit) {
        optionsMenu.setOnMenuItemClickListener {
            callBack(it)
            return@setOnMenuItemClickListener true
        }
    }


    fun launchSettings(action: Int) {
        findNavController().navigate(action)
    }

}