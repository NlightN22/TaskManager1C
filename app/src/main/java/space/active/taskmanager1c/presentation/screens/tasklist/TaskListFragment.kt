package space.active.taskmanager1c.presentation.screens.tasklist

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDirections
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.databinding.FragmentTaskListBinding
import space.active.taskmanager1c.domain.models.TaskDomain
import space.active.taskmanager1c.domain.models.TaskListFilterTypes
import space.active.taskmanager1c.domain.models.TaskListOrderTypes
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.utils.*
import javax.inject.Inject

private const val TAG = "TaskListFragment"

@AndroidEntryPoint
class TaskListFragment : BaseFragment(R.layout.fragment_task_list) {

    private lateinit var binding: FragmentTaskListBinding

    private val viewModel by viewModels<TaskListViewModel>()

    private lateinit var recyclerTasks: TaskListAdapter
    private lateinit var orderMenu: PopupMenu

    @Inject
    lateinit var taskStatusDialog: TaskStatusDialog


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskListBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        recyclerTasks = TaskListAdapter(object : TaskActionListener {
            override fun onTaskStatusClick(taskDomain: TaskDomain) {
                viewModel.checkStatusDialog(taskDomain)
            }

            override fun onTaskClick(taskDomain: TaskDomain) {
                launchTaskDetailed(taskId = taskDomain.id)
            }

            override fun onTaskLongClick(view: View) {
                val curMenu = showMenu(requireContext(), view, R.menu.menu_tasklist_task)
                val taskDomain = view.tag as TaskDomain
                curMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.taskUnread -> {
                            viewModel.changeUnreadTag(taskDomain)
                        }
                    }
                    return@setOnMenuItemClickListener false
                }
            }
        }
        )
        binding.listTasksRV.layoutManager = NotifyingLinearLayoutManager(requireContext())
        binding.listTasksRV.adapter = recyclerTasks
        binding.listUpFAB.hide()

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
        //alert dialog
        viewModel.statusAlertEvent.collectOnStart {
            taskStatusDialog.showDialog(it.second, requireContext()) {
                viewModel.changeTaskStatus(it.first)
            }
        }

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
            val selected: ColorStateList =
                resources.getColorStateList(
                    R.color.task_list_status_completed,
                    resources.newTheme()
                )
            val notSelected: ColorStateList =
                resources.getColorStateList(R.color.bottom_menu_icon, resources.newTheme())
            orderMenu.menu.forEach {
                it.iconTintList = notSelected
            }
            with(orderMenu.menu) {
                when (type) {
                    is TaskListOrderTypes.Name -> {
                        findItem(R.id.orderName).apply {
                            setIconAZState(type.desc, requireContext())
                            setTitleOrder(type.desc, requireContext(), R.string.menu_order_name)
                            iconTintList = selected
                        }
                    }
                    is TaskListOrderTypes.EndDate -> {
                        findItem(R.id.orderEndDate).apply {
                            setIcon09State(type.desc, requireContext())
                            setTitleOrder(type.desc, requireContext(), R.string.menu_order_end_date)
                            iconTintList = selected
                        }
                    }
                    is TaskListOrderTypes.StartDate -> {
                        findItem(R.id.orderStartDate).apply {
                            setIcon09State(type.desc, requireContext())
                            setTitleOrder(
                                type.desc,
                                requireContext(),
                                R.string.menu_order_start_date
                            )
                            iconTintList = selected
                        }
                    }
                    is TaskListOrderTypes.Performer -> {
                        findItem(R.id.orderPerformer).apply {
                            setIconAZState(type.desc, requireContext())
                            setTitleOrder(
                                type.desc,
                                requireContext(),
                                R.string.menu_order_performer
                            )
                            iconTintList = selected
                        }
                    }
                }
            }
        }

        // for autocomplete search
        viewModel.userDomainList.collectOnStart { users ->
            val arrayNames: Array<String> = users.map { it.name }.toTypedArray()
            binding.searchEditText.setSimpleItems(arrayNames)
        }

        // taskDomain list for recycler view
        viewModel.listTask.collectOnStart { request ->
            when (request) {
                is PendingRequest -> {
                    shimmerShow(binding.shimmerTasksRV, binding.listTasksRV, true)
                }
                is SuccessRequest -> {
                    recyclerTasks.taskDomains = request.data
                    binding.listTasksRV.post {
                        shimmerShow(binding.shimmerTasksRV, binding.listTasksRV, false)
                    }
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
        orderMenu.menu.findItem(R.id.orderName)
            .setTitleOrder(false, requireContext(), R.string.menu_order_name)
        orderMenu.menu.findItem(R.id.orderPerformer)
            .setTitleOrder(false, requireContext(), R.string.menu_order_performer)
        orderMenu.menu.findItem(R.id.orderStartDate)
            .setTitleOrder(false, requireContext(), R.string.menu_order_start_date)
        orderMenu.menu.findItem(R.id.orderEndDate)
            .setTitleOrder(false, requireContext(), R.string.menu_order_end_date)

    }

    private fun listeners() {

        binding.listTasksRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var scrollJob: Job? = null
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                // scroll to up - show fab
                if (dy < 0 && recyclerView.canScrollVertically(-1)) {
                    scrollJob?.cancel()
                    scrollJob = lifecycleScope.launch {
                        binding.listUpFAB.show()
                        delay(500)
                        binding.listUpFAB.hide()
                    }
                }
                // scroll to down - hide fab
                if (dy > 0 && recyclerView.canScrollVertically(1)) {
                    binding.listUpFAB.hide()
                }
                // zero position - hide fab
                if (!recyclerView.canScrollVertically(-1)) {
                    binding.listUpFAB.hide()
                }
            }
        })

        binding.listUpFAB.setOnClickListener {
            binding.listTasksRV.scrollToPosition(0)
        }

        binding.searchEditText.addTextChangedListener { editable ->
            viewModel.find(editable)
        }

        binding.searchEditText.setOnItemClickListener { parent, view, position, id ->
            hideKeyboardFrom(requireContext(), binding.searchEditText)
        }

        // options menu
        binding.optionsMenu.setOnClickListener {
            val optionsMenu = showOptionsMenu(binding.optionsMenu)
            optionsMenu?.let {
                setOnOptionsMenuClickListener(it) {
                    when (it.itemId) {
                        R.id.options_settings -> {
                            navigate(TaskListFragmentDirections.actionTaskListFragmentToSettingsFragment())
                        }
                        R.id.options_logout -> {
                            clearUserCredentialsAndExit()
                            super.onDestroy()
                        }
                        R.id.options_about -> {
                            navigate(TaskListFragmentDirections.actionTaskListFragmentToAboutFragment())
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

    private fun showMenu(context: Context, view: View, @MenuRes menuRes: Int): PopupMenu {
        val menu = PopupMenu(context, view)
        menu.inflate(menuRes)
        menu.setForceShowIcon(true)
        menu.gravity = Gravity.END
        menu.show()
        return menu
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
        navigate(direction)
    }
}