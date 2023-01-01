package space.active.taskmanager1c.presentation.screens.tasklist

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.ErrorRequest
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.databinding.FragmentTaskListBinding
import space.active.taskmanager1c.domain.models.Task
import space.active.taskmanager1c.domain.models.TaskListFilterTypes
import space.active.taskmanager1c.domain.models.TaskListOrderTypes
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.utils.setIcon09State
import space.active.taskmanager1c.presentation.utils.setIconAZState

private const val TAG = "TaskListFragment"

@AndroidEntryPoint
class TaskListFragment : BaseFragment(R.layout.fragment_task_list) {

    private lateinit var binding: FragmentTaskListBinding

    private val viewModel by viewModels<TaskListViewModel>()

    private lateinit var recyclerTasks: TaskListAdapter
    private lateinit var orderMenu: PopupMenu

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskListBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        recyclerTasks = TaskListAdapter(object : TaskActionListener {
            override fun onTaskStatusClick(task: Task) {
                viewModel.changeTaskStatus(task)
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

        initOrderMenu()
        observers()
        listeners()
    }

    override fun navigateToLogin() {
        navigate(TaskListFragmentDirections.actionTaskListFragmentToLoginFragment())
    }

    override fun successLogin() {
        viewModel.collectListTasks()
    }

    private fun observers() {


        // start updateJob in MainVM
        viewModel.startUpdateJob.collectOnStart {
            if (it) {
                baseMainVM.updateJob()
            }
        }

        // collect saveId events to change isSaved status
        baseMainVM.savedIdEvent.collectOnStart {
            viewModel.changeIsSending(it)
        }

        // SnackBar observer
        showSnackBar(viewModel.showSnackBar)

        // Save observer
        viewModel.saveTaskEvent.collectOnStart {
            baseMainVM.saveTask(it)
        }

        // order sate for order menu
        viewModel.bottomOrder.collectOnStart { type ->
            with(orderMenu.menu) {
                when (type) {
                    is TaskListOrderTypes.Name -> {
                        findItem(R.id.orderName).setIconAZState(type.desc, requireContext())
                    }
                    is TaskListOrderTypes.EndDate -> {
                        findItem(R.id.orderEndDate).setIcon09State(type.desc, requireContext())
                    }
                    is TaskListOrderTypes.StartDate -> {
                        findItem(R.id.orderStartDate).setIcon09State(
                            type.desc,
                            requireContext()
                        )
                    }
                    is TaskListOrderTypes.Performer -> {
                        findItem(R.id.orderPerformer).setIconAZState(
                            type.desc,
                            requireContext()
                        )
                    }
                }
            }
        }

        // for autocomplete search
        viewModel.userList.collectOnStart { users ->
            val arrayNames: Array<String> = users.map { it.name }.toTypedArray()
            binding.searchEditText.setSimpleItems(arrayNames)
        }

        // task list for recycler view
        viewModel.listTask.collectOnStart { request ->
            when (request) {
                is PendingRequest -> {
                    shimmerShow(binding.shimmerTasksRV, binding.listTasksRV, true)
                }
                is SuccessRequest -> {
                    recyclerTasks.tasks = request.data
                    shimmerShow(binding.shimmerTasksRV, binding.listTasksRV, false)
                }
                is ErrorRequest -> {
                    showSnackBar(UiText.Dynamic(request.exception.message.toString()))
                }
            }
        }

    }

    private fun initOrderMenu() {
        orderMenu = PopupMenu(requireContext(), binding.bottomMenu)
        orderMenu.inflate(R.menu.menu_tasklist_order)
        orderMenu.setForceShowIcon(true)
        orderMenu.gravity = Gravity.START
    }

    private fun listeners() {
        binding.searchEditText.addTextChangedListener { editable ->
            viewModel.find(editable)
        }

        // options menu
        binding.optionsMenu.setOnClickListener {
            val optionsMenu = showOptionsMenu(this.context, binding.optionsMenu)
            optionsMenu?.let {
                setOnOptionsMenuClickListener(it) {
                    when (it.itemId) {
                        R.id.options_settings -> {
                            navigate(TaskListFragmentDirections.actionTaskListFragmentToSettingsFragment())
                        }
                        R.id.options_logout -> {
                            clearUserCredentialsAndExit()
                        }
                    }
                }
            }
        }

        binding.bottomMenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.tasklist_filter -> {
                    showFilterMenu(binding.bottomMenu)
                }
                R.id.tasklist_newTask -> {
                    launchTaskDetailed("")
                }
                R.id.tasklist_order -> {
                    showOrderMenu()
                }
            }
            return@setOnItemSelectedListener true
        }
        binding.backButtonTaskList.setOnClickListener {
            onBackClick()
        }

    }

    private fun showOrderMenu() {
        orderMenu.show()
        orderMenu.setOnMenuItemClickListener {
            with(viewModel) {
                when (it.itemId) {
                    R.id.orderName -> {
                        orderByBottomMenu(TaskListOrderTypes.Name())
                    }
                    R.id.orderPerformer -> {
                        orderByBottomMenu(TaskListOrderTypes.Performer())
                    }
                    R.id.orderStartDate -> {
                        orderByBottomMenu(TaskListOrderTypes.StartDate())
                    }
                    R.id.orderEndDate -> {
                        orderByBottomMenu(TaskListOrderTypes.EndDate())
                    }
                }
            }
            return@setOnMenuItemClickListener false
        }
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
        val direction: NavDirections =
            TaskListFragmentDirections.actionTaskListFragmentToTaskDetailedFragment(taskId)
        findNavController().navigate(
            direction,
            // TODO add animations
        )
    }
}