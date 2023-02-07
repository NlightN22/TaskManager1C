package space.active.taskmanager1c.presentation.screens.login

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.Loading
import space.active.taskmanager1c.coreutils.OnWait
import space.active.taskmanager1c.coreutils.Success
import space.active.taskmanager1c.databinding.FragmentLoginBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.screens.LOGIN_SUCCESSFUL

private const val TAG = "LoginFragment"

class LoginFragment : BaseFragment(R.layout.fragment_login) {

    lateinit var binding: FragmentLoginBinding
    lateinit var previousStateHandle: SavedStateHandle
    private val viewModel: LoginViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentLoginBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        initLoginState()
        observers()
        listeners()
    }

    private fun initLoginState() {
        previousStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        previousStateHandle[LOGIN_SUCCESSFUL] = false
    }

    override fun getBottomMenu(): BottomNavigationView? {
        val bottomNavigationView = binding.bottomMenu.root
        bottomNavigationView.inflateMenu(R.menu.menu_login)
        return bottomNavigationView
    }

    override fun navigateToLogin() {
//         nothing
    }

    override fun successLogin() {
        onBackClick()
    }

    private fun observers() {
        viewModel.viewState.collectOnCreated {
            binding.editTextUsername.setText(it.username)
            binding.editTextPassword.setText(it.password)
            binding.userNameTIL.error = it.userError?.getString(requireContext())
            binding.passTIL.error = it.passError?.getString(requireContext())
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
                    logger.log(TAG, "authState Success")
                    previousStateHandle[LOGIN_SUCCESSFUL] = true
                    successLogin()
                }
            }
        }
    }

    private fun renderLoading(state: Boolean) {
        shimmerShow(binding.loginShimmer, binding.userNameTIL, state)
        shimmerShow(binding.passShimmer, binding.passTIL, state)
        shimmerShow(binding.bottomShimmer, binding.bottomMenu.root, state)
    }

    private fun listeners() {
        binding.editTextPassword.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                auth()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.bottomMenu.root.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.login_ok -> {
                    auth()
                }
                R.id.login_camera -> {
                    // todo add scan credentials
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.optionsMenu.setOnClickListener {
            val optionsMenu = showOptionsMenu(binding.optionsMenu)
            optionsMenu?.let { options ->
                setOnOptionsMenuClickListener(options) {
                    when (it.itemId) {
                        R.id.options_settings -> {
                            navigate(LoginFragmentDirections.actionLoginFragmentToSettingsFragment())
                        }
                        R.id.options_logout -> {
                            clearUserCredentialsAndExit()
                        }
                        R.id.options_about -> {
                            navigate(LoginFragmentDirections.actionLoginFragmentToAboutFragment())
                        }
                    }
                }
            }
        }
    }

    private fun auth() {
        val name = binding.editTextUsername.text?.toString() ?: ""
        val pass = binding.editTextPassword.text?.toString() ?: ""
        viewModel.auth(name, pass)
    }
}