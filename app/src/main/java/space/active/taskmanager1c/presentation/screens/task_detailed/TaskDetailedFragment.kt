package space.active.taskmanager1c.presentation.screens.task_detailed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.databinding.FragmentTaskDetailedBinding
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.domain.models.TaskChangesEvents
import space.active.taskmanager1c.domain.models.UserDomain
import space.active.taskmanager1c.domain.models.UserDomain.Companion.fromDialogItems
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.utils.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.inject.Inject

private const val TAG = "TaskDetailedFragment"

@AndroidEntryPoint
class TaskDetailedFragment : BaseFragment(R.layout.fragment_task_detailed) {

    lateinit var binding: FragmentTaskDetailedBinding
    private val viewModel by viewModels<TaskDetailedViewModel>()

    @Inject
    lateinit var taskStatusDialog: TaskStatusDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentTaskDetailedBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        observers()
        listeners()
    }

    override fun getBottomMenu(): BottomNavigationView? {
        val bottomNavigationView = binding.bottomMenu.root
        bottomNavigationView.inflateMenu(R.menu.menu_detailed)
        return bottomNavigationView
    }

    override fun navigateToLogin() {
        navigate(TaskDetailedFragmentDirections.actionTaskDetailedFragmentToLoginFragment())
    }

    override fun successLogin() {
        val taskId = TaskDetailedFragmentArgs.fromBundle(requireArguments()).taskId
        viewModel.setTaskFlow(taskId ?: "")
    }

    private fun observers() {
        //observe status dialog
        viewModel.statusAlertEvent.collectOnStart {
            taskStatusDialog.showDialog(it.second, requireContext()) {
                viewModel.saveEditChanges(it.first)
            }
        }


        //observe save event
        viewModel.validationEvent.collectOnStart {
            if (it) {
                onBackClick()
            }
        }

        //validate errors observer
        viewModel.taskErrorState.collectOnStart { state ->
            binding.taskTitleTIL.error = state.title?.getString(requireContext())
            binding.taskDeadlineTIL.error = state.endDate?.getString(requireContext())
            binding.taskPerformerTIL.error = state.performer?.getString(requireContext())
            binding.taskAuthorTIL.error = state.author?.getString(requireContext())
        }


        renderState(viewModel)

        // Render enabled fields
        renderFields(viewModel)

        // SnackBar observer
        showSnackBar(viewModel.showSnackBar)

        // Save observer
        viewModel.saveNewTaskEvent.collectOnStart {
            baseMainVM.saveTask(it)
            if (it is SaveEvents.Breakable) {
                onBackClick()
            }
        }

        // Dialog observers
        viewModel.showDialogEvent.collectOnStart { event ->
            when (event) {
                is PerformerDialog -> {
                    event.listUsers?.let {
                        SingleChooseDialog.show(
                            parentFragmentManager,
                            it,
                            ok = false,
                            cancel = true
                        )
                    }
                }
                is CoPerformersDialog -> {
                    event.listDialogItems?.let {
                        MultiChooseDialog.show(
                            parentFragmentManager,
                            it,
                            ok = true,
                            cancel = true,
                            REQUEST_COPERFOMREFRS
                        )
                    }
                }
                is ObserversDialog -> {
                    event.listDialogItems?.let {
                        MultiChooseDialog.show(
                            parentFragmentManager,
                            it,
                            ok = true,
                            cancel = true,
                            REQUEST_OBSERVERS
                        )
                    }
                }
                is DatePicker -> {
                    showDatePicker()
                }
                is EditTitleDialog -> {
                    event.dialogState?.let {
                        EditTextDialog.show(
                            parentFragmentManager,
                            it,
                            REQUEST_TITLE
                        )
                    }
                }
                is EditDescriptionDialog -> {
                    event.dialogState?.let {
                        EditTextDialog.show(
                            parentFragmentManager,
                            it,
                            REQUEST_DESCRIPTION
                        )
                    }
                }
            }
        }

        // todo delete
        //Expand cards observers
//        viewModel.expandState.collectOnStart { expandState ->
//            renderMainDetailed(expandState.main)
//            renderDescription(expandState.description)
//        }

        setupMultiChooseDialog()
        setupSingleChooseDialog()
        setupEditTextListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {

        binding.bottomMenu.root.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.detailed_cancel -> {
                    viewModel.checkStatusDialog(TaskChangesEvents.Status(false))
                }
                R.id.detailed_ok -> {
                    viewModel.checkStatusDialog(TaskChangesEvents.Status(true))
                }
                R.id.menu_save -> {
                    viewModel.saveNewTask()
                }
                R.id.menu_cancel -> {
                    onBackClick()
                }
                R.id.detailed_messages -> {
                    val taskId = TaskDetailedFragmentArgs.fromBundle(requireArguments()).taskId
                    navigate(TaskDetailedFragmentDirections.actionTaskDetailedFragmentToMessagesFragment(taskId))
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.taskTitleDetailed.setOnClickListener {
            viewModel.showDialog(EditTitleDialog(null))
        }

        binding.taskDeadline.setOnClickListener {
            viewModel.showDialog(DatePicker)
        }

        binding.taskPerformer.setOnClickListener {
            viewModel.showDialog(PerformerDialog(null))
        }

        binding.taskCoPerformers.setOnClickListener {
            viewModel.showDialog(CoPerformersDialog(null))
        }

        binding.taskObservers.setOnClickListener {
            viewModel.showDialog(ObserversDialog(null))
        }

        binding.taskDescription.setOnClickListener {
            viewModel.showDialog(EditDescriptionDialog(null))
        }

        // todo delete
//        val detailedViewList = binding.mainDetailCard.getAllViews()
//        detailedViewList.forEach {
//            if (it.id == R.id.taskPerformer) {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeMainDetailed() },
//                    actionDown = {
//                        viewModel.expandMainDetailed()
//                    },
//                    actionClick = { viewModel.showDialog(PerformerDialog(null)) },
//                    scrollView = binding.detailedScrollView
//                )
//            } else if (it.id == R.id.taskCoPerformers) {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeMainDetailed() },
//                    actionDown = {
//                        viewModel.expandMainDetailed()
//                    },
//                    actionClick = { viewModel.showDialog(CoPerformersDialog(null)) },
//                    scrollView = binding.detailedScrollView
//                )
//            }else if (it.id == R.id.taskObservers) {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeMainDetailed() },
//                    actionDown = {
//                        viewModel.expandMainDetailed()
//                    },
//                    actionClick = { viewModel.showDialog(ObserversDialog(null)) },
//                    scrollView = binding.detailedScrollView
//                )
//            } else if (it.id == R.id.taskDeadline) {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeMainDetailed() },
//                    actionDown = { viewModel.expandMainDetailed() },
//                    actionClick = { viewModel.showDialog(DatePicker) },
//                    scrollView = binding.detailedScrollView
//                )
//            }else if (it.id == R.id.expandMainDetailCard) {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeMainDetailed() },
//                    actionDown = { viewModel.expandMainDetailed() },
//                    actionClick = { viewModel.expandCloseMainDetailed() },
//                    scrollView = binding.detailedScrollView
//                )
//            } else {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeMainDetailed() },
//                    actionDown = { viewModel.expandMainDetailed() },
//                    scrollView = binding.detailedScrollView
//                )
//            }
//        }
//
//        val descriptionViewList = binding.taskDescriptionCard.getAllViews()
//        descriptionViewList.forEach {
//            if (it.id == R.id.taskDescription) {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeDescription() },
//                    actionDown = {
//                        viewModel.expandDescription()
//                    },
//                    actionClick = { viewModel.showDialog(EditDescriptionDialog(null)) },
//                    scrollView = binding.detailedScrollView
//                )
//            } else if (it.id == R.id.expandTaskDescriptionCard) {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeDescription() },
//                    actionDown = { viewModel.expandDescription() },
//                    actionClick = { viewModel.expandCloseDescription() },
//                    scrollView = binding.detailedScrollView
//                )
//            } else {
//                it.setSwipeListener(
//                    actionUp = { viewModel.closeDescription() },
//                    actionDown = { viewModel.expandDescription() },
//                    scrollView = binding.detailedScrollView
//                )
//            }
//        }

        binding.backButtonTaskDetailed.setOnClickListener {
            onBackClick()
        }
    }

    private fun View.getAllViews(): List<View> {
        return if (this !is ViewGroup || this.childCount == 0) {
            listOf(this)
        } else {
            val listViews = arrayListOf<View>()
            listViews.add(this)
            this.forEach {
                if (it is ViewGroup) {
                    listViews.addAll(it.getAllViews())
                } else {
                    listViews.add(it)
                }
            }
            listViews
        }
    }

    private fun View.setSwipeListener(
        actionUp: () -> Unit, actionDown: () -> Unit,
        scrollView: ScrollView? = null
    ) {
        this.setOnTouchListener(object :
            OnSwipeTouchListener(this.context, scrollView) {
            override fun onSwipeUp() {
                super.onSwipeUp()
                actionUp()
            }

            override fun onSwipeDown() {
                super.onSwipeDown()
                actionDown()
            }

        }
        )
    }

    private fun View.setSwipeListener(
        actionUp: () -> Unit,
        actionDown: () -> Unit,
        actionClick: () -> Unit,
        scrollView: ScrollView? = null
    ) {
        this.setOnTouchListener(object :
            OnSwipeTouchListener(this.context, scrollView) {
            override fun onSwipeUp() {
                actionUp()
                super.onSwipeUp()
            }

            override fun onSwipeDown() {
                actionDown()
                super.onSwipeDown()
            }

            override fun onClick() {
                actionClick()
                super.onClick()
            }
        }
        )
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(getString(R.string.date_picker_title))
            .build()
        datePicker.show(this.childFragmentManager, "DatePicker")
        datePicker.addOnPositiveButtonClickListener {
            val convertedPickedDate: ZonedDateTime = it.millisecToZonedDateTime()
            logger.log(TAG, "convertedPickedDate: $convertedPickedDate")
            val currentZoneId = ZoneId.systemDefault()
            logger.log(TAG, "currentZoneId: $currentZoneId")
            viewModel.saveEditChanges(
                TaskChangesEvents.DeadLine(
                    convertedPickedDate
                )
            )
        }
        datePicker.addOnNegativeButtonClickListener {
            showSnackBar(UiText.Resource(R.string.toast_date_not_selected))
        }
        datePicker.addOnCancelListener {
            showSnackBar(UiText.Resource(R.string.toast_date_not_selected))
        }
    }


    // todo delete
//    private fun renderMainDetailed(state: Boolean) {
//        binding.taskCoPerformersCard.isVisible = state
//        binding.taskObserversCard.isVisible = state
//        binding.expandMainDetailCard.forEach {
//            if (it is ImageView) {
//                it.setImageResource(
//                    if (state) {
//                        R.drawable.ic_expandable_up_arrow
//                    } else {
//                        R.drawable.ic_expandable_down_arrow
//                    }
//                )
//            }
//        }
//    }


    // todo delete
//    private fun renderDescription(state: Boolean) {
//        binding.taskDescription.maxLines = if (state) {
//            100
//        } else {
//            3
//        }
//        binding.taskBaseObjectCard.isVisible = state
//        binding.taskBaseCard.isVisible = state
//        binding.taskInnerCardView.isVisible = state
//        binding.expandTaskDescriptionCard.forEach {
//            if (it is ImageView) {
//                it.setImageResource(
//                    if (state) {
//                        R.drawable.ic_expandable_up_arrow
//                    } else {
//                        R.drawable.ic_expandable_down_arrow
//                    }
//                )
//            }
//        }
//    }

    private fun setupMultiChooseDialog() {
        val listener: CustomInputDialogListener = { requestKey, listItems ->
            when (requestKey) {
                REQUEST_COPERFOMREFRS -> viewModel.saveEditChanges(
                    TaskChangesEvents.CoPerformers(
                        listItems.fromDialogItems()
                    )
                )
                REQUEST_OBSERVERS -> viewModel.saveEditChanges(
                    TaskChangesEvents.Observers(
                        listItems.fromDialogItems()
                    )
                )
            }
        }
        MultiChooseDialog.setupListener(
            parentFragmentManager,
            this,
            REQUEST_COPERFOMREFRS,
            listener
        )
        MultiChooseDialog.setupListener(parentFragmentManager, this, REQUEST_OBSERVERS, listener)
    }

    private fun setupSingleChooseDialog() {
        SingleChooseDialog.setupListener(parentFragmentManager, this) {
            it?.let {
                viewModel.saveEditChanges(TaskChangesEvents.Performer(UserDomain.fromDialogItem(it)))
            }
        }
    }

    private fun setupEditTextListener() {
        val listener: CustomEditTextDialogListener = { requestKey, text ->
            when (requestKey) {
                REQUEST_TITLE -> viewModel.saveEditChanges(TaskChangesEvents.Title(text ?: ""))
                REQUEST_DESCRIPTION -> viewModel.saveEditChanges(
                    TaskChangesEvents.Description(
                        text ?: ""
                    )
                )
            }
        }
        EditTextDialog.setupListener(parentFragmentManager, this, REQUEST_TITLE, listener)
        EditTextDialog.setupListener(parentFragmentManager, this, REQUEST_DESCRIPTION, listener)
    }

    companion object {
        private const val REQUEST_COPERFOMREFRS = "REQUEST_COPERFOMREFRS"
        private const val REQUEST_OBSERVERS = "REQUEST_OBSERVERS"
        private const val REQUEST_TITLE = "REQUEST_TITLE"
        private const val REQUEST_DESCRIPTION = "REQUEST_DESCRIPTION"
    }
}