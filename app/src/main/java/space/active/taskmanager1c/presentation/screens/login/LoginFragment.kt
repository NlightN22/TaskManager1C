package space.active.taskmanager1c.presentation.screens.login

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.navigation.fragment.findNavController
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentLoginBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment

class LoginFragment: BaseFragment(R.layout.fragment_login) {

    lateinit var binding: FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

//        incoming()
//        observers()
        listeners()
    }

    private fun incoming() {
        TODO("Not yet implemented")
    }

    private fun observers() {
        TODO("Not yet implemented")
    }

    private fun listeners() {
        binding.bottomMenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.login_ok -> {
                    launchMainScreen(true)
                }
                R.id.login_camera -> {
                    launchSettings(R.id.action_loginFragment2_to_settingsFragment2)//                    TODO("Not yet implemented")
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.optionsMenu.setOnClickListener {
            val optionsMenu = showOptionsMenu(this.context,binding.optionsMenu)
            optionsMenu?.let { optionsMenu ->
                setOnOptionsMenuClickListener(optionsMenu) {
                    when (it.itemId) {
                        R.id.options_settings -> {
                            launchSettings(R.id.action_loginFragment2_to_settingsFragment2)
                        }
                        R.id.options_logout -> {}
                    }
                }
            }
        }
    }


    private fun launchMainScreen(isSignedIn: Boolean) {
        if (isSignedIn) {
            findNavController().navigate(R.id.action_loginFragment2_to_main_nav_graph2)
        }
    }

}