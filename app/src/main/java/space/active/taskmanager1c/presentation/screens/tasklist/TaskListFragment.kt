package space.active.taskmanager1c.presentation.screens.tasklist

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.databinding.FragmentTaskListBinding
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.TaskListFilterTypes
import space.active.taskmanager1c.domain.models.User
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
            viewModel.userList.collectLatest { users->
                val arrayNames: Array<String> = users.map { it.name }.toTypedArray()
                binding.searchEditText.setSimpleItems(arrayNames)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.listTask.collectLatest { request ->
                when (request) {
                    is PendingRequest -> {
                        showShimmer()
                    }
                    is SuccessRequest -> {
                        recyclerTasks.tasks = request.data
                        showRecyclerView()
                    }
                    is ErrorRequest -> {
                        showSnackBar(request.exception.message.toString(), binding.root)
                    }
                }
            }
        }

    }

    private fun listeners() {
        binding.searchEditText.addTextChangedListener { editable ->
            viewModel.find(editable)
        }

        // options menu
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
                    val viewMenu = binding.bottomMenu
                    logger.log(TAG, "viewMenu $viewMenu")
                    showFilterMenu(viewMenu)
                }
                R.id.tasklist_newTask -> {
                }
            }
            return@setOnItemSelectedListener true
        }
        binding.backButtonTaskList.setOnClickListener {
            onBackClick()
        }

    }

    private fun showShimmer() {
        binding.listTasksRV.visibility = View.GONE
        binding.shimmerTasksRV.apply {
            visibility = View.VISIBLE
            startShimmer()
        }
    }

    private fun showRecyclerView() {
        binding.shimmerTasksRV.apply {
            stopShimmer()
            visibility = View.GONE
        }
        binding.listTasksRV.visibility = View.VISIBLE
    }

    private fun showFilterMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.menu_tasklist_filter)
        popupMenu.setForceShowIcon(true)
        popupMenu.gravity = Gravity.END
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            with(viewModel) {
                when (it.itemId) {
                    R.id.iDo -> {
                        filterByBottomMenu(TaskListFilterTypes.IDo)
                    }
                    R.id.iDelegate -> {
                        filterByBottomMenu(TaskListFilterTypes.IDelegate)
                    }
                    R.id.iDidNtCheck -> {
                        filterByBottomMenu(TaskListFilterTypes.IDidNtCheck)
                    }
                    R.id.iObserve -> {
                        filterByBottomMenu(TaskListFilterTypes.IObserve)
                    }
                    R.id.iDidNtRead -> {
                        filterByBottomMenu(TaskListFilterTypes.IDidNtRead)
                    }
                    R.id.allTasks -> {
                        filterByBottomMenu(TaskListFilterTypes.All)
                    }
                }
            }
            return@setOnMenuItemClickListener false
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