package space.active.taskmanager1c.presentation.screens.tasklist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentTaskListBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment

@AndroidEntryPoint
class TaskListFragment : BaseFragment(R.layout.fragment_task_list) {

    lateinit var binding: FragmentTaskListBinding
    private val viewModel by viewModels<TaskListViewModel>()
    lateinit var recyclerTasks: TaskListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskListBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        recyclerTasks = TaskListAdapter()
        binding.listTasksRV.adapter = recyclerTasks

        //        incoming()
        observers()
        listeners()
    }

    private fun observers() {
        lifecycleScope.launchWhenStarted {
            viewModel.listTask.collectLatest {
                recyclerTasks.tasks = it
            }
        }

    }

    private fun listeners() {

        binding.optionsMenu.setOnClickListener {
            val optionsMenu = showOptionsMenu(this.context, binding.optionsMenu)
            optionsMenu?.let { optionsMenu ->
                setOnOptionsMenuClickListener(optionsMenu) {
                    when (it.itemId) {
                        R.id.options_settings -> {
                            launchSettings(R.id.action_taskListFragment_to_settingsFragment)
                        }
                        R.id.options_logout -> {}
                    }
                }
            }
        }

        binding.bottomMenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.tasklist_filter -> {
                    launchTaskDetailed("test")
                }
                R.id.tasklist_newTask -> {
                    launchSetting()
                }
            }
            return@setOnItemSelectedListener true
        }
    }

    private fun launchTaskDetailed(taskId: String) {
        findNavController().navigate(R.id.action_taskListFragment_to_taskDetailedFragment)
    }

    private fun launchSetting() {
        findNavController().navigate(R.id.action_taskListFragment_to_settingsFragment)
    }
}