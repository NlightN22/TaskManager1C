package space.active.taskmanager1c.presentation.screens.task_detailed

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
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

        observers()
        listeners()
    }

    private fun observers() {
        lifecycleScope.launchWhenStarted {
            viewModel.taskState.collectLatest { taskState ->

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

    private fun listeners() {
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
        binding.expandMainDetailCard.rotation = if (state) {180F} else {0F}
        // todo change arrow direction
    }

    private fun renderDescription(state: Boolean) {
        binding.taskBaseObjectCard.isVisible = state
        binding.taskBaseCard.isVisible = state
        binding.taskInnerCardView.isVisible = state
        binding.expandTaskDescriptionCard.rotation = if (state) {180F} else {0F}
        // todo change arrow direction
    }

}