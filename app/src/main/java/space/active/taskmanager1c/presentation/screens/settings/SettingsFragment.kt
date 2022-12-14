package space.active.taskmanager1c.presentation.screens.settings

import android.os.Bundle
import android.view.View
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentSettingsBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment

class SettingsFragment: BaseFragment(R.layout.fragment_settings) {

    lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)
    }
}