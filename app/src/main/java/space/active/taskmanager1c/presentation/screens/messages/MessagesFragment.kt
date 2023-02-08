package space.active.taskmanager1c.presentation.screens.messages

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.Loading
import space.active.taskmanager1c.coreutils.PendingRequest
import space.active.taskmanager1c.coreutils.SuccessRequest
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.databinding.FragmentMessagesBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.utils.hideKeyboardFrom

private const val TAG = "AboutFragment"

class MessagesFragment : BaseFragment(R.layout.fragment_messages) {

    //todo copy message or reply

    lateinit var binding: FragmentMessagesBinding
    lateinit var messagesAdapter: MessagesAdapter

    private val viewModel by viewModels<MessagesViewModel>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentMessagesBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        initRV()
        observers()
        listeners()
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

        binding.backButton.setOnClickListener {
            onBackClick()
        }
    }

    private fun observers() {
        // title observer
        viewModel.messagesViewState.collectOnStart { state ->
            binding.taskTitleDetailed.setText(state.title)
            binding.taskNumberDetailed.setText(state.number)
            binding.taskDateDetailed.setText(state.date)
            state.status?.let { binding.taskStatus.setText(it) }
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