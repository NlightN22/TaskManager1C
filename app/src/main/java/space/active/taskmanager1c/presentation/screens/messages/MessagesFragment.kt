package space.active.taskmanager1c.presentation.screens.messages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.Loading
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.databinding.FragmentMessagesBinding
import space.active.taskmanager1c.domain.use_case.setState
import space.active.taskmanager1c.domain.use_case.setText
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.utils.hideKeyboardFrom

private const val TAG = "AboutFragment"

class MessagesFragment : BaseFragment(R.layout.fragment_messages) {

    lateinit var binding: FragmentMessagesBinding
    lateinit var messagesAdapter: MessagesAdapter

    private val viewModel by viewModels<MessagesViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMessagesBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        initRV()
        initTitle()
        observers()
        listeners()
    }

    private fun initTitle() {
        binding.titleMessages.setState(false)
    }

    private fun initRV() {
        messagesAdapter = MessagesAdapter { message ->
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("simple text", message.text)
            clipboard.setPrimaryClip(clip)
            showSnackBar(UiText.Resource(R.string.message_copied_to_clipboard, message.text))
        }
        binding.messagesRV.adapter = messagesAdapter
    }

    override fun getBottomMenu(): BottomNavigationView? {
        return null
    }

    private fun listeners() {

        binding.messageTIL.setEndIconOnClickListener {
            val text: String = binding.messageInput.text?.toString() ?: ""
            viewModel.sendMessage(text)
            hideKeyboardFrom(requireContext(), binding.messageInput)
        }

        binding.titleMessages.backButton.setOnClickListener {
            onBackClick()
        }
    }

    private fun observers() {
        // title observer
        viewModel.taskTitleViewState.collectOnStart { state ->
            binding.titleMessages.setText(state)
        }

        // SnackBar observer
        showSnackBar(viewModel.showSnackBar)

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

        // messages observer
        viewModel.messageList.collectOnStart { request ->
            when (request) {
                is SuccessRequest -> {
                    messagesAdapter.submitList(request.data)
                    binding.messagesRV.scrollToPosition(0)
                    shimmerShow(binding.shimmerMessagesRV, binding.messagesRV, false)
                }
                is PendingRequest -> {
                    shimmerShow(binding.shimmerMessagesRV, binding.messagesRV, true)
                }
                else -> {}
            }
        }

    }

    override fun navigateToLogin() {
        navigate(MessagesFragmentDirections.actionMessagesFragmentToLoginFragment())
    }

    override fun successLogin() {
        val taskId = MessagesFragmentArgs.fromBundle(requireArguments()).taskId
        viewModel.successLogin(taskId)
    }

}