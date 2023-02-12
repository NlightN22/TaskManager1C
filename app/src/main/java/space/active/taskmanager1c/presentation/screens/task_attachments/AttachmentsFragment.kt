package space.active.taskmanager1c.presentation.screens.task_attachments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.FragmentAttachmentsBinding
import space.active.taskmanager1c.domain.models.InternalStorageFile
import space.active.taskmanager1c.presentation.screens.BaseFragment
import space.active.taskmanager1c.presentation.screens.bottom_sheet_dialog.AttachmentBottomDialog
import space.active.taskmanager1c.presentation.screens.bottom_sheet_dialog.AttachmentDialogListener
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
        attachmentsAdapter = AttachmentsAdapter(object : AttachmentsAdapter.ClickViews {
            override fun onItemClick(view: View, item: InternalStorageFile) {
                viewModel.clickItem(item)
            }

            override fun onOptionsMenuClick(view: View, item: InternalStorageFile) {
                showOptionsMenu(item, view)
            }

            override fun onLongClick(view: View, item: InternalStorageFile) {
                showOptionsMenu(item, view)
            }
        })
        registerForContextMenu(binding.listAttachmentsRV)
        binding.listAttachmentsRV.adapter = attachmentsAdapter
    }

    private fun observers() {
        viewModel.deleteFileEvent.collectOnStart {
            deleteFile(it)
        }

        viewModel.openFileEvent.collectOnStart {
            openFile(it)
        }

        viewModel.listItems.collectOnStart {
            attachmentsAdapter.submitList(it)
        }
    }

    private fun listeners() {
        val sheetListener: AttachmentDialogListener = { requestKey, buttonString ->
            logger.log(TAG, "result from sheet dialog: $buttonString")
        }

        AttachmentBottomDialog.setupListener(
            parentFragmentManager,
            this,
            BOTTOM_SHEET_DIALOG_REQUEST,
            sheetListener
        )

        binding.bottomMenu.root.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.attachmentsAdd -> {
                    openAddChooser()
                }
            }
            return@setOnItemSelectedListener true
        }

        binding.backButton.root.setOnClickListener {
            onBackClick()
        }
    }

    private fun showOptionsMenu(item: InternalStorageFile, view: View) {
        val renderedMenu = renderOptionsMenu(view, item)
        renderedMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.attachmentItemOpen -> {
                    viewModel.openCachedFile(item)
                    return@setOnMenuItemClickListener true
                }
                R.id.attachmentItemDelete -> {
                    viewModel.deleteCachedFile(item)
                    return@setOnMenuItemClickListener true
                }
                R.id.attachmentItemUpload -> {
                    viewModel.uploadFileToServer(item)
                    return@setOnMenuItemClickListener true
                }
                R.id.attachmentItemDownload -> {
                    viewModel.downloadFileFromServer(item)
                    return@setOnMenuItemClickListener true
                }
            }
            return@setOnMenuItemClickListener false
        }
        renderedMenu.show()
    }

    private fun renderOptionsMenu(view: View, item: InternalStorageFile): PopupMenu {
        val popupMenu = PopupMenu(requireContext(), view)
        if (item.cached) {
            popupMenu.menu.apply {
                add(
                    R.menu.options_menu_attachment_item,
                    R.id.attachmentItemOpen,
                    1,
                    R.string.open_item
                )
                add(
                    R.menu.options_menu_attachment_item,
                    R.id.attachmentItemDelete,
                    2,
                    R.string.delete_item
                )
            }
        }
        if (item.notUploaded) {
            popupMenu.menu.add(
                R.menu.options_menu_attachment_item,
                R.id.attachmentItemUpload,
                3,
                R.string.upload_item
            )
        }
        if (!item.cached) {
            popupMenu.menu.add(
                R.menu.options_menu_attachment_item,
                R.id.attachmentItemDownload,
                4,
                R.string.download_item
            )
        }
        return popupMenu
    }

    private fun deleteFile(internalStorageFile: InternalStorageFile) {
        internalStorageFile.uri?.toFile()?.let { file ->
            if (file.exists()) {
                file.delete()
            }
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

    private fun openAddChooser() {
        val taskId = AttachmentsFragmentArgs.fromBundle(requireArguments()).taskId
        AttachmentBottomDialog.show(parentFragmentManager, BOTTOM_SHEET_DIALOG_REQUEST, taskId)
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

    companion object {
        private const val BOTTOM_SHEET_DIALOG_REQUEST = "SHEET_REQUEST"
    }
}