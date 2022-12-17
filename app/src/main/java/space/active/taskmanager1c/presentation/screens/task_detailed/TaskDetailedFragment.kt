package space.active.taskmanager1c.presentation.screens.task_detailed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.OnSwipeTouchListener
import space.active.taskmanager1c.databinding.FragmentTaskDetailedBinding
import space.active.taskmanager1c.domain.models.User
import space.active.taskmanager1c.domain.models.User.Companion.fromDialogItems
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.screens.mainactivity.MainViewModel
import space.active.taskmanager1c.domain.models.SaveEvents
import space.active.taskmanager1c.domain.models.TaskChangesEvents
import space.active.taskmanager1c.presentation.utils.*
import java.util.*


private const val TAG = "TaskDetailedFragment"

@AndroidEntryPoint
class TaskDetailedFragment : BaseFragment(R.layout.fragment_task_detailed) {

    lateinit var binding: FragmentTaskDetailedBinding
    private val viewModel by viewModels<TaskDetailedViewModel>()
    private val mainVM by viewModels<MainViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTaskDetailedBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

        income()
        observers()
        listeners()
    }

    private fun income() {
        val taskId = TaskDetailedFragmentArgs.fromBundle(requireArguments()).taskId
        if (taskId != null) {
            viewModel.getTaskFlow(taskId)
        }
    }

    private fun observers() {
        lifecycleScope.launchWhenCreated {
            viewModel.taskState.collectLatest { taskState ->
                binding.taskTitleDetailed.setText(taskState.title)
                binding.taskNumberDetailed.text = taskState.number
                binding.taskStatus.text = taskState.status.name
                binding.taskDateDetailed.text = taskState.startDate
                binding.taskDeadline.setText(taskState.deadLine)
                binding.taskAuthor.setText(taskState.author)
                binding.taskDaysEnd.setText(taskState.daysEnd)
                binding.taskPerformer.setText(taskState.performer)
                binding.taskCoPerformers.setText(taskState.coPerfomers)
                binding.taskObservers.setText(taskState.observers)
                binding.taskDescription.setText(taskState.description)
                binding.taskBaseObject.setText(taskState.taskObject)
                binding.taskMain.setText(taskState.mainTask)
                binding.taskInner.setText(taskState.innerTasks)
            }
        }

        // Render enabled fields
        lifecycleScope.launchWhenCreated {
            viewModel.enabledFields.collectLatest { fieldsState ->
                // Title
                binding.taskTitleCardView.setState(enabled = fieldsState.title)
                binding.taskTitleTIL.setState(
                    enabled = fieldsState.title,
                    editable = fieldsState.title
                )
                binding.taskDateDetailed.setColorState(fieldsState.title)
                binding.taskStatus.setColorState(fieldsState.title)
                binding.taskNumberDetailed.setColorState(fieldsState.title)
                // End date
                binding.taskDeadlineCardView.setState(enabled = fieldsState.deadLine)
                binding.taskDeadlineTIL.setState(enabled = fieldsState.deadLine)
                if (fieldsState.deadLine) {
                    binding.taskDeadline.setOnClickListener {
                        showDatePicker()
                    }
                }
                // Performer
                binding.taskPerformerCard.setState(enabled = fieldsState.performer)
                binding.taskPerformerTIL.setState(enabled = fieldsState.performer)
                if (fieldsState.performer) {
                    binding.taskPerformer.setOnClickListener {
                        viewModel.showDialog(PerformerDialog(null))
                    }
                }
                // CoPerformers
                binding.taskCoPerformersCard.setState(enabled = fieldsState.coPerfomers)
                binding.taskCoPerformersTIL.setState(enabled = fieldsState.coPerfomers)
                if (fieldsState.coPerfomers) {
                    binding.taskCoPerformers.setOnClickListener {
                        viewModel.showDialog(CoPerformersDialog(null))
                    }
                }
                // Observers
                binding.taskObserversCard.setState(enabled = fieldsState.observers)
                binding.taskObserversTIL.setState(enabled = fieldsState.observers)
                if (fieldsState.observers) {
                    binding.taskObservers.setOnClickListener {
                        viewModel.showDialog(ObserversDialog(null))
                    }
                }
                // Descriptions
                binding.taskDescriptionCardView.setState(enabled = fieldsState.description)
                binding.taskDescriptionTIL.setState(
                    enabled = fieldsState.description,
                    editable = fieldsState.description
                )
                // bottom menu state
                binding.bottomMenu.menu.findItem(R.id.detailed_cancel).isVisible = fieldsState.bottomPerformer
            }
        }

        // SnackBar observer
        lifecycleScope.launchWhenStarted {
            viewModel.showSnackBar.collectLatest {
                showSnackBar(it, binding.root)
            }
        }

        // Save observer
        lifecycleScope.launchWhenStarted {
            viewModel.saveTaskEvent.collectLatest {
                mainVM.saveTask(it)
                if (it is SaveEvents.Breakable) {
                    onBackClick()
                }
            }
        }

        // Dialog observers
        lifecycleScope.launchWhenStarted {
            viewModel.showDialogEvent.collectLatest { event ->
                when (event) {
                    is PerformerDialog -> {
                        if (event.listUsers != null) {
                            SingleChooseDialog.show(
                                parentFragmentManager,
                                event.listUsers, // todo replace to dialog item in class
                                ok = false,
                                cancel = true
                            )
                        }
                    }
                    is CoPerformersDialog -> {
                        if (event.listDialogItems != null) {
                            MultiChooseDialog.show(
                                parentFragmentManager,
                                event.listDialogItems,
                                ok = true,
                                cancel = true,
                                REQUEST_COPERFOMREFRS
                            )
                        }
                    }
                    is ObserversDialog -> {
                        if (event.listDialogItems != null) {
                            MultiChooseDialog.show(
                                parentFragmentManager,
                                event.listDialogItems,
                                ok = true,
                                cancel = true,
                                REQUEST_OBSERVERS
                            )
                        }
                    }
                }
            }
        }


        //Expand cards observers
        lifecycleScope.launchWhenStarted {
            viewModel.expandState.collectLatest { expandState ->
                renderMainDetailed(expandState.main)
                renderDescription(expandState.description)
            }
        }

        setupMultiChooseDialog()
        setupSingleChooseDialog()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {

        binding.bottomMenu.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.detailed_cancel -> {
                    viewModel.saveChangesSmart(TaskChangesEvents.Status(false))
                }
                R.id.detailed_ok -> {
                    viewModel.saveChangesSmart(TaskChangesEvents.Status(true))
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.taskTitleDetailed.addTextChangedListener {
            viewModel.saveChangesSmart(TaskChangesEvents.Title(it.toString()))
        }

        binding.taskDescription.addTextChangedListener {
            viewModel.saveChangesSmart(TaskChangesEvents.Description(it.toString()))
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
            findNavController().popBackStack()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder
            .datePicker()
            .setTitleText(getString(R.string.date_picker_title))
            .build()
        datePicker.show(this.childFragmentManager, "DatePicker")
        datePicker.addOnPositiveButtonClickListener {
            viewModel.saveChangesSmart(TaskChangesEvents.EndDate(Date(it)))
        }
        datePicker.addOnNegativeButtonClickListener {
            showSnackBar(getString(R.string.toast_date_not_selected), binding.root)
        }
        datePicker.addOnCancelListener {
            showSnackBar(getString(R.string.toast_date_not_selected), binding.root)
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
                REQUEST_COPERFOMREFRS -> viewModel.saveChangesSmart(
                    TaskChangesEvents.CoPerformers(
                        listItems.fromDialogItems()
                    )
                )
                REQUEST_OBSERVERS -> viewModel.saveChangesSmart(
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
                viewModel.saveChangesSmart(TaskChangesEvents.Performer(User.fromDialogItem(it)))
            }
        }
    }

    companion object {
        private const val REQUEST_COPERFOMREFRS = "REQUEST_COPERFOMREFRS"
        private const val REQUEST_OBSERVERS = "REQUEST_OBSERVERS"
    }
}