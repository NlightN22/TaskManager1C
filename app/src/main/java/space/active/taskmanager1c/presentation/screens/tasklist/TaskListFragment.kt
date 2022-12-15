package space.active.taskmanager1c.presentation.screens.tasklist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentTaskListBinding
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.screens.mainactivity.MainViewModel

private const val TAG = "TaskListFragment"

@AndroidEntryPoint
class TaskListFragment : BaseFragment(R.layout.fragment_task_list) {

    lateinit var binding: FragmentTaskListBinding

    private val viewModel by viewModels<TaskListViewModel>()
    private val mainVM by viewModels<MainViewModel>()

    lateinit var recyclerTasks: TaskListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskListBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        // Start autoupdate job
        mainVM.updateJob()

        recyclerTasks = TaskListAdapter(object : TaskActionListener {
            override fun onTaskStatusClick(task: Task) {
                TODO("Not yet implemented")
            }

            override fun onTaskClick(task: Task) {
                launchTaskDetailed(taskId = task.id)
            }

            override fun onTaskLongClick(task: Task) {
                TODO("Not yet implemented")
            }
        }

        )
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
                        R.id.options_logout -> {
                            onBackClick()
                        }
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
        binding.backButtonTaskList.setOnClickListener {
            onBackClick()
        }

    }

    private fun launchTaskDetailed(taskId: String) {
        val direction =
            TaskListFragmentDirections.actionTaskListFragmentToTaskDetailedFragment(taskId)
        findNavController().navigate(
            direction,
            // TODO add animations
        )
    }

    private fun launchSetting() {
        findNavController().navigate(R.id.action_taskListFragment_to_settingsFragment)
    }
}