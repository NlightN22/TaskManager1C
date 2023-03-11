package space.active.taskmanager1c.presentation.screens.task_detailed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.*
import space.active.taskmanager1c.databinding.FragmentTaskDetailedBinding
import space.active.taskmanager1c.domain.models.FragmentDeepLinks
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

        viewModel.shareTaskEvent.collectOnStart {
            shareUri(it)
        }

        viewModel.openClickableTask.collectOnStart {
            navigate(it)
        }

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
            binding.title.taskTitleTIL.error = state.title?.getString(requireContext())
            binding.taskDeadlineTIL.error = state.endDate?.getString(requireContext())
            binding.taskPerformerTIL.error = state.performer?.getString(requireContext())
            binding.taskAuthorTIL.error = state.author?.getString(requireContext())
        }

        renderState(viewModel)

        // Render enabled fields
        renderFields(viewModel)

        renderUnread(viewModel)

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

        setupMultiChooseDialog()
        setupSingleChooseDialog()
        setupEditTextListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {

        binding.bottomMenu.root.setOnItemSelectedListener { menuItem ->
            val taskId = TaskDetailedFragmentArgs.fromBundle(requireArguments()).taskId
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
                    navigate(
                        TaskDetailedFragmentDirections.actionTaskDetailedFragmentToMessagesFragment(
                            taskId
                        )
                    )
                }
                R.id.detailed_attach -> {
                    navigate(
                        TaskDetailedFragmentDirections.actionTaskDetailedFragmentToAttachmentsFragment(
                            taskId
                        )
                    )
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.title.taskNameET.setOnClickListener {
            viewModel.showDialog(EditTitleDialog(null))
        }
        binding.title.shareButton.setOnClickListener {
            viewModel.shareTaskClick()
        }

        binding.title.backButton.setOnClickListener {
            onBackClick()
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

        binding.taskMain.setOnClickListener {
            viewModel.clickOnMainTask(binding.taskMain.text.toString())
        }

        binding.taskInner.setOnClickListener {
            viewModel.clickOnInnerTasks(binding.taskInner.text.toString())
        }


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