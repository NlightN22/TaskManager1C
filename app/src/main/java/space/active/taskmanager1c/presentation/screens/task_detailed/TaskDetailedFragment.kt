package space.active.taskmanager1c.presentation.screens.task_detailed

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentTaskDetailedBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import javax.inject.Inject


class TaskDetailedFragment: BaseFragment(R.layout.fragment_task_detailed) {

    lateinit var binding: FragmentTaskDetailedBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskDetailedBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)
    }
}