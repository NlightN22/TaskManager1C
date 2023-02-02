package space.active.taskmanager1c.presentation.screens.task_attachments

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.fragment.app.viewModels
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentAttachmentsBinding
import space.active.taskmanager1c.presentation.screens.BaseFragment
import java.io.File
import java.util.*

private const val TAG = "AttachmentsFragment"

class AttachmentsFragment: BaseFragment(R.layout.fragment_attachments) {

    private val viewModel: AttachmentsViewModel by viewModels()

    lateinit var binding: FragmentAttachmentsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAttachmentsBinding.bind(view)
        clearBottomMenuItemIconTintList(binding.bottomMenu)

    }

    override fun successLogin() {
        // todo init vm
    }

    override fun navigateToLogin() {
        navigate(AttachmentsFragmentDirections.actionAttachmentsFragmentToLoginFragment())
    }

    fun getType(file: File) {
        val uri: Uri = Uri.fromFile(file)
        val cR: ContentResolver = requireContext().contentResolver
        val mime = cR.getType(uri)
    }

    fun File.getMimeType(fallback: String = "*/*"): String {
        return MimeTypeMap.getFileExtensionFromUrl(toString())
            ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase(Locale.ROOT)) }
            ?: fallback
    }
}