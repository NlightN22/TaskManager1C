package space.active.taskmanager1c.presentation.screens.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.Loading
import space.active.taskmanager1c.coreutils.OnWait
import space.active.taskmanager1c.coreutils.Success
import space.active.taskmanager1c.databinding.FragmentLoginBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment

private const val TAG = "LoginFragment"

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        observers()
        listeners()
    }


    private fun observers() {
        viewModel.authState.collectOnStart { state ->
            when (state) {
                is OnWait -> {
                    renderLoading(false)
                }
                is Loading -> {
                    renderLoading(true)
                }
                is Success -> {
                    launchMainScreen(true)
                }
            }
        }
    }

    private fun renderLoading(state: Boolean) {
        shimmerShow(binding.loginShimmer, binding.userNameTIL, state)
        shimmerShow(binding.passShimmer, binding.passTIL, state)
        shimmerShow(binding.bottomShimmer, binding.bottomMenu, state)
    }

    private fun listeners() {
        binding.bottomMenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.login_ok -> {
                    val name = binding.editTextUsername.text?.toString() ?: ""
                    val pass = binding.editTextPassword.text?.toString() ?: ""
                    viewModel.auth(name, pass)
                }
                R.id.login_camera -> {
                    // todo add scan credentials
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.optionsMenu.setOnClickListener {
            val optionsMenu = showOptionsMenu(this.context, binding.optionsMenu)
            optionsMenu?.let { optionsMenu ->
                setOnOptionsMenuClickListener(optionsMenu) {
                    when (it.itemId) {
                        R.id.options_settings -> {
                            launchSettings(R.id.action_loginFragment2_to_settingsFragment2)
                        }
                        R.id.options_logout -> {
                            onBackClick()
                        }
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