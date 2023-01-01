package space.active.taskmanager1c.presentation.screens.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.Loading
import space.active.taskmanager1c.coreutils.OnWait
import space.active.taskmanager1c.coreutils.Success
import space.active.taskmanager1c.databinding.FragmentLoginBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.screens.BaseViewModel
import space.active.taskmanager1c.presentation.screens.LOGIN_SUCCESSFUL

private const val TAG = "LoginFragment"

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    lateinit var binding: FragmentLoginBinding
    lateinit var previousStateHandle: SavedStateHandle
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        initLoginState()
        observers()
        listeners()
    }

    private  fun initLoginState() {
        previousStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        previousStateHandle[LOGIN_SUCCESSFUL] = false
    }

    override fun navigateToLogin() {
        // nothing
    }

    override fun successLogin() {
        onBackClick()
    }

    private fun observers() {
        viewModel.viewState.collectOnStart {
            binding.editTextUsername.setText(it.username)
            binding.editTextPassword.setText(it.password)
        }

        viewModel.authState.collectOnStart { state ->
            when (state) {
                is OnWait -> {
                    renderLoading(false)
                }
                is Loading -> {
                    renderLoading(true)
                }
                is Success -> {
                    previousStateHandle[LOGIN_SUCCESSFUL] = true
                    successLogin()
                    // todo delete
//                    launchMainScreen(true)
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
            optionsMenu?.let { options ->
                setOnOptionsMenuClickListener(options) {
                    when (it.itemId) {
                        R.id.options_settings -> {
                            navigate(LoginFragmentDirections.actionLoginFragmentToSettingsFragment())
                        }
                        R.id.options_logout -> {
                            clearUserCredentialsAndExit()
                        }
                    }
                }
            }
        }
    }

    // todo delete
    private fun launchMainScreen(isSignedIn: Boolean) {
        if (isSignedIn) {

//            previousStateHandle.set(LOGIN_SUCCESSFUL, true)
        }
    }

}