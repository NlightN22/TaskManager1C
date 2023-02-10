package space.active.taskmanager1c.presentation.screens.task_attachments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentAttachmentsBinding
import space.active.taskmanager1c.domain.models.InternalStorageFile
import space.active.taskmanager1c.presentation.screens.BaseFragment
import java.io.File
import java.util.*

private const val TAG = "AttachmentsFragment"

class AttachmentsFragment : BaseFragment(R.layout.fragment_attachments) {

    private val viewModel: AttachmentsViewModel by viewModels()

    lateinit var binding: FragmentAttachmentsBinding
    lateinit var attachmentsAdapter: AttachmentsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAttachmentsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        initRV()
        observers()
        listeners()
    }

    private fun initRV() {
        attachmentsAdapter = AttachmentsAdapter {
            viewModel.clickItem(it)
        }
        binding.listAttachmentsRV.adapter = attachmentsAdapter
    }

    private fun observers() {
        viewModel.openFileEvent.collectOnStart {
            openFile(it)
        }

        viewModel.listItems.collectOnStart {
            attachmentsAdapter.submitList(it)
        }
    }

    private fun listeners() {
        binding.backButton.root.setOnClickListener {
            onBackClick()
        }
    }

    private fun openFile(internalStorageFile: InternalStorageFile) {
        val intent = Intent(Intent.ACTION_VIEW)
        internalStorageFile.uri?.let { uri ->
            try {
                val mimeType = internalStorageFile.filename.getMimeType()
                val uriProvider = FileProvider.getUriForFile(
                    requireContext(),
                    "space.active.taskmanager1c.fileprovider",
                    uri.toFile(),
                    internalStorageFile.filename
                )
                logger.log(TAG, "uriProvider: $uriProvider\nmimeType: $mimeType")
                intent.setDataAndType(uriProvider, mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val shareIntent = Intent.createChooser(intent, null)
                startActivity(shareIntent)
            } catch (e: Exception) {
                exceptionHandler(e)
            }
        }
    }

    override fun getBottomMenu(): BottomNavigationView? {
        val bottomNavigationView = binding.bottomMenu.root
        bottomNavigationView.inflateMenu(R.menu.menu_attachments)
        return bottomNavigationView
    }

    override fun successLogin() {
        val taskId = AttachmentsFragmentArgs.fromBundle(requireArguments()).taskId
        viewModel.collectStorageItems(taskId)
    }

    override fun navigateToLogin() {
        navigate(AttachmentsFragmentDirections.actionAttachmentsFragmentToLoginFragment())
    }

    private fun File.getMimeType(fallback: String = "*/*"): String {
        val extension = this.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase(Locale.ROOT))
            ?: fallback
    }

    private fun String.getMimeType(fallback: String = "*/*"): String {
        val extension = this.substringAfterLast(".")
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase(Locale.ROOT))
            ?: fallback
    }

}