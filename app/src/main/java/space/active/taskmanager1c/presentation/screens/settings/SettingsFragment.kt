package space.active.taskmanager1c.presentation.screens.settings

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentSettingsBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.utils.setState

private const val TAG = "SettingsFragment"

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private val viewModel by viewModels<SettingsViewModel>()

    lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSettingsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        loginStateToViewModel()
        observers()
        listeners()
    }

    private fun loginStateToViewModel() {
        viewModel.setSettingsViewState(getLoginState())
    }

    override fun getBottomMenu() : BottomNavigationView {
        val bottomNavigationView = binding.bottomMenu.root
        bottomNavigationView.inflateMenu(R.menu.menu_save_cancel)
        return bottomNavigationView
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

        viewModel.visibleState.collectOnCreated {
            binding.settingsUsername.isVisible = it.userName
            binding.settingsUserId.isVisible = it.userId
            binding.settingsServerAddressCard.isVisible = it.serverAddress
            binding.switchSkipStatusAlert.isVisible = it.skipStatusAlert
        }

        viewModel.viewState.collectOnCreated {
            binding.settingsUsernameET.setText(it.userName)
            binding.settingsUserIdET.setText(it.userId)
            binding.settingsServerAddressET.setText(it.serverAddress)
            logger.log(TAG, "viewState editServerAddress ${it.editServerAddress}")
            binding.settingsServerAddressTIL.error = it.addressError?.getString(requireContext())
            binding.settingsServerAddressCard.setState(enabled = it.editServerAddress)
            binding.settingsServerAddressTIL.setState(enabled = it.editServerAddress, editable = it.editServerAddress)
            binding.switchSkipStatusAlert.isChecked = it.skipStatusAlert
        }
    }

    private fun listeners() {
        binding.switchSkipStatusAlert.setOnCheckedChangeListener { buttonView, isChecked ->
            viewModel.changeStatusAlert(isChecked)
        }

        binding.settingsServerAddressET.addTextChangedListener {
            viewModel.changeServerAddressState(it?.toString() ?: "")
        }

        binding.bottomMenu.root.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_save -> {
                    viewModel.saveSettings()
                }
                R.id.menu_cancel -> {
                    onBackClick()
                }
            }
            return@setOnItemSelectedListener true
        }
    }
}