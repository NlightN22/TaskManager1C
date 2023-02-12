package space.active.taskmanager1c.presentation.screens.bottom_sheet_dialog

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import space.active.taskmanager1c.coreutils.logger.Logger
import space.active.taskmanager1c.data.repository.FilesRepositoryImpl
import space.active.taskmanager1c.databinding.DialogBottomSheetBinding
import space.active.taskmanager1c.domain.use_case.ExceptionHandler
import space.active.taskmanager1c.presentation.utils.MultiChooseDialog
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


private const val TAG = "AttachmentBottomDialog"
typealias AttachmentDialogListener = (requestKey: String, buttonString: String?) -> Unit

@AndroidEntryPoint
class AttachmentBottomDialog : BottomSheetDialogFragment() {

    private lateinit var binding: DialogBottomSheetBinding
    private lateinit var requestKey: String

    private lateinit var taskId: String

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var exceptionHandler: ExceptionHandler

    private val createPhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                emitAndClose("Photo saved successfully")
            } else {
                logger.log(TAG, "Failed to save photo")
            }
        }

    private val selectFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                getFilenameForSelectedFile(it)?.let { filename ->
                    lifecycleScope.launch {
                        val isSuccess = saveFileToInternalStorage(it, filename)
                        if (isSuccess) {
                            emitAndClose("File saved successfully")
                        } else {
                            logger.log(TAG, "Failed to save file")
                        }
                    }
                }
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
                        getFilenameForSelectedFile(uri)?.let { filename ->
                            lifecycleScope.launch {
                                val isSuccess = saveFileToInternalStorage(uri, filename)
                                if (isSuccess) {
                                    emitAndClose("Photo saved successfully")
                                } else {
                                    logger.log(TAG, "Failed to save Photo")
                                }
                            }
                        }
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
        taskId = requireArguments().getString(TASK_ID_TAG)!!
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

    private fun observers() {

    }

    private fun listeners() {
        binding.buttonTakePhoto.setOnClickListener {
            createPhoto.launch(
                getTmpFileUri(
                    getNewPhotoFilename()
                )
            )
        }

        binding.buttonSelectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectPhoto.launch(intent)
        }

        binding.buttonSelectFile.setOnClickListener {
            try {
                selectFile.launch("*/*")
            } catch (e: Exception) {
                exceptionHandler(e)
            }
        }
    }


    private suspend fun saveFileToInternalStorage(uri: Uri, pathToSave: File): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                pathToSave.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
                inputStream?.close()
                true
            } catch (e: IOException) {
                exceptionHandler(e)
                false
            }
        }
    }

    private fun getFilenameForSelectedFile(uri: Uri): File? {
        try {
            val fileName = getFileName(uri)
            val fileId = UUID.randomUUID().toString()
            val finalName = "$fileId@$fileName"
            val taskFolder = FilesRepositoryImpl.getTaskCacheDir(requireContext(), taskId)
            return File(taskFolder, finalName)
        } catch (e: Exception) {
            exceptionHandler(e)
            return null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0) {
                        result = it.getString(columnIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            result = uri.path?.substringAfterLast("/")
        }
        return result
    }

    private suspend fun savePhotoToInternalStorage(pathToSave: File, bmp: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                pathToSave.outputStream().use { stream ->
                    if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                        throw IOException("Couldn't save file.")
                    }
                }
                true
            } catch (e: IOException) {
                exceptionHandler(e)
                false
            }
        }
    }

    private fun getTmpFileUri(cachedFileName: File): Uri {
        return FileProvider.getUriForFile(
            requireContext(),
            "space.active.taskmanager1c.fileprovider",
            cachedFileName
        )
    }

    private fun getNewPhotoFilename(): File {
        val taskFolder = FilesRepositoryImpl.getTaskCacheDir(requireContext(), taskId)
        return File(taskFolder, createNewPhotoName())
    }

    private fun createNewPhotoName(): String {
        // name has 2 parts: fileId @ filename.jpg
        // new fileId must unique
        // fileId replaced by server after upload
        val fileId = UUID.randomUUID().toString()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date())
        val filename = "$timeStamp.jpg"
        val finalName = "$fileId@$filename"
        return finalName
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