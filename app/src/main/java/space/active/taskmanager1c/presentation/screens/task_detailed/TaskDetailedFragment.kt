package space.active.taskmanager1c.presentation.screens.task_detailed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.OnSwipeTouchListener
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.databinding.FragmentTaskDetailedBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.utils.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


private const val TAG = "TaskDetailedFragment"

@AndroidEntryPoint
class TaskDetailedFragment : BaseFragment(R.layout.fragment_task_detailed) {

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var toasts: Toasts

    lateinit var binding: FragmentTaskDetailedBinding
    private val viewModel by viewModels<TaskDetailedViewModel>()

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
        lifecycleScope.launchWhenCreated {
            viewModel.enabledFields.collectLatest { fieldsState ->
//                Log.d("TestViewState", "fieldsState $fieldsState")
                // Title
                binding.taskTitleCardView.setState(enabled = fieldsState.title)
                binding.taskTitleTIL.setState(
                    enabled = fieldsState.title,
                    editable = fieldsState.title
                )
                binding.taskDateDetailed.setColorState(fieldsState.title)
                binding.taskNumberDetailed.setColorState(fieldsState.title)
//                binding.taskTitleDetailed.enabled(fieldsState.title)
                // End date
                binding.taskDeadlineCardView.setState(enabled = fieldsState.deadLine)
                binding.taskDeadlineTIL.setState(enabled = fieldsState.deadLine)
                if (fieldsState.deadLine) {
                    binding.taskDeadline.setOnClickListener {
                        showDatePicker()
                    }
                }
//                binding.taskDeadline.enabled(fieldsState.deadLine)
                // Performer
                binding.taskPerformerCard.setState(enabled = fieldsState.performer)
                binding.taskPerformerTIL.setState(enabled = fieldsState.performer)
                if (fieldsState.performer) {
                    binding.taskPerformer.setOnClickListener {
                        viewModel.showDialogMultipleSelectUsers() //todo change to single
                    }
                }
//                binding.taskPerformer.enabled(fieldsState.performer)
                // CoPerformers
                binding.taskCoPerformersCard.setState(enabled = fieldsState.coPerfomers)
                binding.taskCoPerformersTIL.setState(enabled = fieldsState.coPerfomers)
//                binding.taskCoPerformers.enabled(fieldsState.coPerfomers)
                // Observers
                binding.taskObserversCard.setState(enabled = fieldsState.observers)
                binding.taskObserversTIL.setState(enabled = fieldsState.observers)
//                binding.taskObservers.enabled(fieldsState.observers)
                // Descriptions
                binding.taskDescriptionCardView.setState(enabled = fieldsState.description)
                binding.taskDescriptionTIL.setState(
                    enabled = fieldsState.description,
                    editable = fieldsState.description
                )
//                binding.taskDescription.enabled(fieldsState.description)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.showUsersToMultiSelect.collectLatest { dialogItems->
                showMultiChooseDialog(dialogItems)
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.showUsersToSelectOne.collectLatest { dialogItems ->
                showSingleChooseDialog(dialogItems.map { it.text })
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.expandState.collectLatest { expandState ->
                renderMainDetailed(expandState.main)
                renderDescription(expandState.description)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.inputMessage.collectLatest {

            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.changeState.collectLatest {

            }
        }
        setupMultiChooseDialog()
        setupSingleChooseDialog()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {
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
        binding.expandMainDetailCard.setOnClickListener {
            viewModel.expandCloseMainDetailed()
        }
        binding.expandTaskDescriptionCard.setOnClickListener {
            viewModel.expandCloseDescription()
        }
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
            val dateFormatter = SimpleDateFormat("dd-MM-yyyy")
            val date = dateFormatter.format(Date(it))
            toasts.toast(getString(R.string.toast_date_selected, date))
        }
        datePicker.addOnNegativeButtonClickListener {
            toasts.toast(getString(R.string.toast_date_not_selected))
        }
        datePicker.addOnCancelListener {
            toasts.toast(getString(R.string.toast_date_not_selected))
        }
    }

    private fun renderMainDetailed(state: Boolean) {
        binding.taskCoPerformersCard.isVisible = state
        binding.taskObserversCard.isVisible = state
        binding.expandMainDetailCard.rotation = if (state) {
            180F
        } else {
            0F
        }
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
        binding.expandTaskDescriptionCard.rotation = if (state) {
            180F
        } else {
            0F
        }
    }

    private fun showMultiChooseDialog(listItems: List<MultiChooseDialog.DialogItem>) {
        MultiChooseDialog.show(parentFragmentManager, listItems, ok = true, cancel = true)
    }

    private fun setupMultiChooseDialog() {
        MultiChooseDialog.setupListener(parentFragmentManager, this) {
            it?.let {
                logger.log(TAG, "setupMultiChooseDialog $it")
            }
        }
    }

    private fun showSingleChooseDialog(listUsers: List<String>) {
        SingleChooseDialog.show(parentFragmentManager, listUsers, ok = false, cancel = true)
    }

    private fun setupSingleChooseDialog() {
        SingleChooseDialog.setupListener(parentFragmentManager, this) {
            it?.let {
                logger.log(TAG, "setupMultipleChooseDialog $it")
            }
        }
    }
}