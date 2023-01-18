package space.active.taskmanager1c.presentation.screens.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentSettingsBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.utils.setState

private const val TAG = "SettingsFragment"

@AndroidEntryPoint
class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private val viewModel by viewModels<SettingsViewModel>()

    lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)


        loginStateToViewModel()
        observers()
        listeners()
    }

    private fun loginStateToViewModel() {
        val previousDestId = findNavController().previousBackStackEntry?.destination?.id
        previousDestId?.let {
            viewModel.setServerAddressEditState(it == R.id.loginFragment)
        }
    }

    override fun navigateToLogin() {
        //nothing
    }

    override fun successLogin() {
        //nothing
    }

    private fun observers() {
        viewModel.saveEvent.collectOnStart {
            if (it) {onBackClick()}
        }

        viewModel.viewState.collectOnStart {
            binding.settingsUsernameET.setText(it.userName)
            binding.settingsUserIdET.setText(it.userId)
            binding.settingsServerAddressET.setText(it.serverAddress)
            logger.log(TAG, "viewState editServerAddress ${it.editServerAddress}")
            binding.settingsServerAddressTIL.error = it.addressError?.getString(requireContext())
            binding.settingsServerAddressCard.setState(enabled = it.editServerAddress)
            binding.settingsServerAddressTIL.setState(enabled = it.editServerAddress, editable = it.editServerAddress)

        }
    }

    private fun listeners() {
        binding.bottomMenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_save -> {
                    viewModel.saveSettings(binding.settingsServerAddressET.text.toString())
                }
                R.id.menu_cancel -> {
                    onBackClick()
                }
            }
            return@setOnItemSelectedListener true
        }
    }
}