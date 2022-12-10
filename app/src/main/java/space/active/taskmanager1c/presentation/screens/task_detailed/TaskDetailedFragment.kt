package space.active.taskmanager1c.presentation.screens.task_detailed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.OnSwipeTouchListener
import space.active.taskmanager1c.databinding.FragmentTaskDetailedBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment

private const val TAG = "TaskDetailedFragment"

@AndroidEntryPoint
class TaskDetailedFragment : BaseFragment(R.layout.fragment_task_detailed) {

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
        lifecycleScope.launchWhenStarted {
            viewModel.taskState.collectLatest { taskState ->
                binding.taskTitle.setText(taskState.title)
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
            viewModel.saveState.collectLatest {

            }
        }
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
        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
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
        binding.taskBaseObjectCard.isVisible = state
        binding.taskBaseCard.isVisible = state
        binding.taskInnerCardView.isVisible = state
        binding.expandTaskDescriptionCard.rotation = if (state) {
            180F
        } else {
            0F
        }
    }

}