package space.active.taskmanager1c.presentation.screens.task_detailed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
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
import java.util.*
import javax.inject.Inject


private const val TAG = "TaskDetailedFragment"

@AndroidEntryPoint
class TaskDetailedFragment : BaseFragment(R.layout.fragment_task_detailed) {



    lateinit var binding: FragmentTaskDetailedBinding
    lateinit var messagesAdapter: MessagesAdapter
    private val viewModel by viewModels<TaskDetailedViewModel>()

    @Inject lateinit var taskStatusDialog: TaskStatusDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskDetailedBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        messagesAdapter = MessagesAdapter()
        binding.messagesRV.adapter = messagesAdapter

        observers()
        listeners()
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
            taskStatusDialog.showDialog(it.second,requireContext()) {
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


        // message sender
        viewModel.sendMessageEvent.collectOnStart { progress ->
            when (progress) {
                is Loading -> {
                    binding.messageTIL.isEndIconVisible = false
                }
                else -> {
                    binding.messageTIL.isEndIconVisible = true
                    binding.messageInput.setText("")
                }
            }
        }

        renderState(viewModel)

        // Render enabled fields
        renderFields(viewModel)

        // messages observer
        viewModel.messageList.collectOnStart { request ->
            when (request) {
                is SuccessRequest -> {
                    messagesAdapter.messages = request.data
                    shimmerShow(binding.shimmerMessagesRV, binding.messagesRV, false)
                }
                is PendingRequest -> {
                    shimmerShow(binding.shimmerMessagesRV, binding.messagesRV, true)
                }
                else -> {}
            }
        }

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

        //Expand cards observers
        viewModel.expandState.collectOnStart { expandState ->
            renderMainDetailed(expandState.main)
            renderDescription(expandState.description)
        }

        setupMultiChooseDialog()
        setupSingleChooseDialog()
        setupEditTextListener()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {

        binding.messageTIL.setEndIconOnClickListener {
            val text: String = binding.messageInput.text?.toString() ?: ""
            viewModel.sendMessage(text)
        }

        binding.bottomMenu.setOnItemSelectedListener { menuItem ->
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
            }
            return@setOnItemSelectedListener true
        }

        binding.taskTitleDetailed.changeListener {
            viewModel.saveEditChanges(TaskChangesEvents.Title(it))
        }

        binding.taskDescription.changeListener {
            viewModel.saveEditChanges(TaskChangesEvents.Description(it))
        }

        binding.mainDetailCard.setOnTouchListener(object :
            OnSwipeTouchListener(binding.mainDetailCard.context) {
            override fun onSwipeDown() {
                super.onSwipeDown()
                viewModel.expandMainDetailed()
            }

            override fun onSwipeUp() {
                super.onSwipeUp()
                viewModel.closeMainDetailed()
            }
        })

        binding.taskDescriptionCard.setOnTouchListener(object :
            OnSwipeTouchListener(binding.taskDescriptionCard.context) {
            override fun onSwipeDown() {
                super.onSwipeDown()
                viewModel.expandDescription()
            }

            override fun onSwipeUp() {
                super.onSwipeUp()
                viewModel.closeDescription()
            }
        }
        )

        binding.expandMainDetailCard.setOnTouchListener(object :
            OnSwipeTouchListener(binding.expandMainDetailCard.context) {
            override fun onSwipeDown() {
                super.onSwipeDown()
                viewModel.expandMainDetailed()
            }

            override fun onSwipeUp() {
                super.onSwipeUp()
                viewModel.closeMainDetailed()
            }

            override fun onClick() {
                super.onClick()
                viewModel.expandCloseMainDetailed()
            }
        }
        )

        binding.expandTaskDescriptionCard.setOnTouchListener(object :
            OnSwipeTouchListener(binding.taskDescriptionCard.context) {
            override fun onSwipeDown() {
                super.onSwipeDown()
                viewModel.expandDescription()
            }

            override fun onSwipeUp() {
                super.onSwipeUp()
                viewModel.closeDescription()
            }

            override fun onClick() {
                super.onClick()
                viewModel.expandCloseDescription()
            }
        }
        )

        binding.backButtonTaskDetailed.setOnClickListener {
            onBackClick()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(getString(R.string.date_picker_title))
            .build()
        datePicker.show(this.childFragmentManager, "DatePicker")
        datePicker.addOnPositiveButtonClickListener {
            viewModel.saveEditChanges(
                TaskChangesEvents.EndDate(
                    it.millisecToZonedDateTime()
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

    private fun renderMainDetailed(state: Boolean) {
        binding.taskCoPerformersCard.isVisible = state
        binding.taskObserversCard.isVisible = state
        binding.expandMainDetailCardImage.setImageResource(
            if (state) {
                R.drawable.ic_expandable_up_arrow
            } else {
                R.drawable.ic_expandable_down_arrow
            }
        )
    }

    private fun renderDescription(state: Boolean) {
        binding.taskDescription.maxLines = if (state) {
            100
        } else {
            3
        }
        binding.taskBaseObjectCard.isVisible = state
        binding.taskBaseCard.isVisible = state
        binding.taskInnerCardView.isVisible = state
        binding.expandTaskDescriptionCardImage.setImageResource(
            if (state) {
                R.drawable.ic_expandable_up_arrow
            } else {
                R.drawable.ic_expandable_down_arrow
            }
        )
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
        EditTextDialog.setupListener(parentFragmentManager,this, REQUEST_TITLE, listener)
        EditTextDialog.setupListener(parentFragmentManager,this, REQUEST_DESCRIPTION, listener)
    }

    companion object {
        private const val REQUEST_COPERFOMREFRS = "REQUEST_COPERFOMREFRS"
        private const val REQUEST_OBSERVERS = "REQUEST_OBSERVERS"
        private const val REQUEST_TITLE = "REQUEST_TITLE"
        private const val REQUEST_DESCRIPTION = "REQUEST_DESCRIPTION"
    }
}