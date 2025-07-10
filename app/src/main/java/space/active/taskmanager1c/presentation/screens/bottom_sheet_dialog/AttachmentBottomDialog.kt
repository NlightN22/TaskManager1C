package space.active.taskmanager1c.presentation.screens.bottom_sheet_dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import space.active.taskmanager1c.R
import space.active.taskmanager1c.coreutils.UiText
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.databinding.DialogBottomSheetBinding
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.presentation.utils.Toasts
import space.active.taskmanager1c.presentation.utils.dialogs.MultiChooseDialog
import javax.inject.Inject


private const val TAG = "AttachmentBottomDialog"
typealias AttachmentDialogListener = (requestKey: String, buttonString: String?) -> Unit


@AndroidEntryPoint
class AttachmentBottomDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomSheetBinding
    private lateinit var requestKey: String

    private val viewModel: AttachmentBottomDialogViewModel by viewModels()

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var exceptionHandler: ExceptionHandler

    @Inject
    lateinit var toasts: Toasts

    private val createPhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                viewModel.finishSave("Photo saved successfully")
            } else {
                toasts(UiText.Resource(R.string.bottom_sheet_error_save_file))
            }
        }

    private val selectFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                viewModel.saveSelectedExternalFile(uri)
            }
        }

    private val selectPhoto =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.let { intent ->
                    intent.data?.let { uri ->
                        viewModel.saveSelectedExternalFile(uri)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArgs()
        observers()
        listeners()
    }

    private fun initArgs() {
        requestKey = requireArguments().getString(REQUEST_TAG)!!
        val taskId = requireArguments().getString(TASK_ID_TAG)!!
        viewModel.initArgs(taskId)
    }

    private fun emitAndClose(result: String) {
        emitResult(result)
        close()
    }

    private fun emitResult(result: String) {
        parentFragmentManager.setFragmentResult(
            requestKey,
            bundleOf(RESPONSE_TAG to result)
        )
    }

    private fun close() {
        parentFragmentManager.beginTransaction().remove(this).commit()
    }


    private val selectMultiplePhotos =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
            uris.forEach { viewModel.saveSelectedExternalFile(it) }
        }

    private val selectMultipleFiles =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
            uris.forEach { viewModel.saveSelectedExternalFile(it) }
        }

    private fun observers() {
        viewModel.saveNewPhotoEvent.collectOnStart { uri ->
            wrapIntentStartActivity {
                createPhoto.launch(uri)
            }
        }
        viewModel.selectNewPhotoEvent.collectOnStart { mime ->
            wrapIntentStartActivity {
                selectMultiplePhotos.launch(mime)
            }
        }
        viewModel.selectNewFileEvent.collectOnStart { mimeType ->
            wrapIntentStartActivity {
                selectMultipleFiles.launch(arrayOf(mimeType))
            }
        }
        viewModel.saveFinishedEvent.collectOnStart { result ->
            emitAndClose(result)
        }


    }

    private fun <T> Flow<T>.collectOnStart(listener: (T) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            this@collectOnStart.collectLatest {
                listener(it)
            }
        }
    }

    private fun listeners() {
        binding.buttonTakePhoto.setOnClickListener {
            viewModel.clickNewPhoto()
        }

        binding.buttonSelectPhoto.setOnClickListener {
            viewModel.clickSelectNewPhoto()
        }

        binding.buttonSelectFile.setOnClickListener {
            viewModel.clickSelectNewFile()
        }
    }

    private fun wrapIntentStartActivity(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            exceptionHandler(e)
        }
    }

    companion object {
        private val TAG = MultiChooseDialog::class.java.simpleName
        private const val REQUEST_TAG = "REQUEST"
        private const val TASK_ID_TAG = "TASK_ID"
        private const val RESPONSE_TAG = "RESPONSE"

        fun show(
            manager: FragmentManager,
            requestKey: String,
            taskId: String
        ) {
            val dialogFragment = AttachmentBottomDialog()
            dialogFragment.arguments =
                bundleOf(
                    REQUEST_TAG to requestKey,
                    TASK_ID_TAG to taskId
                )
            dialogFragment.show(manager, TAG)
        }

        fun setupListener(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            requestKey: String,
            listener: AttachmentDialogListener
        ) {
            manager.setFragmentResultListener(
                requestKey,
                lifecycleOwner
            ) { _, result ->
                listener.invoke(requestKey, result.getString(RESPONSE_TAG))
            }
        }
    }
}