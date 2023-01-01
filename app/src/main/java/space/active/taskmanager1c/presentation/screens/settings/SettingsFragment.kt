package space.active.taskmanager1c.presentation.screens.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentSettingsBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private val viewModel by viewModels<SettingsViewModel>()

    lateinit var binding: FragmentSettingsBinding
    // todo
    // Server address
    // Username
    // Logging
    // не отображать если нет прав в списке

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        observers()
        listeners()
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
            binding.settingsServerAddressTIL.error = it.addressError?.getString(requireContext())
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