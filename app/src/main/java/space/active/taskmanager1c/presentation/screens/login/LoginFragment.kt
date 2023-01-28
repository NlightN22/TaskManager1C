package space.active.taskmanager1c.presentation.screens.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
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
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "$this Created")
        binding = FragmentLoginBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        initLoginState()
        observers()
        listeners()
    }

    private fun initLoginState() {
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
            binding.userNameTIL.error = it.userError?.getString(requireContext())
            binding.passTIL.error = it.passError?.getString(requireContext())
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val job = this.coroutineContext.job
                logger.log(TAG, "job: ${job} is Active: ${job.isActive}")
                viewModel.authState.collectLatest { state ->
                    logger.log(TAG, "auth collector: ${state.toString()}")
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

    override fun onResume() {
        Log.d(TAG, "$this Resume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "$this Pause")
        super.onPause()
    }

    override fun onDetach() {
        Log.d(TAG, "$this Detach")
        super.onDetach()
    }

    override fun onDestroy() {
        Log.d(TAG, "$this Destroy")
        super.onDestroy()
    }
}