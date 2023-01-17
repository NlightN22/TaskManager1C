package space.active.taskmanager1c.presentation.utils


import android.content.res.Resources.NotFoundException
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.DialogEditTextBinding

typealias CustomEditTextDialogListener = (requestKey: String, text: String?) -> Unit

private const val TAG = "EditTextDialog"

class EditTextDialog : DialogFragment(R.layout.dialog_edit_text) {

    lateinit var binding: DialogEditTextBinding
    lateinit var requestKey: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogEditTextBinding.bind(view)

        val dialogState: EditTextDialogStates =
            arguments?.getParcelable<EditTextDialogStates>(BUNDLE_TAG) ?: EditTextDialogStates()

        requestKey = requireArguments().getString(ARG_REQUEST_KEY)!!

        dialogState.text?.let { binding.dialogET.setText(it) }
        dialogState.hint?.let { binding.dialogTIL.hint = getString(it) }

        dialogState.maxLength?.let {
            val maxLength = try {
                resources.getInteger(it)
            } catch (e: NotFoundException) {
                it
            }
            Log.d(TAG, "maxLength $maxLength")
            val filterLength = arrayOf(InputFilter.LengthFilter(maxLength))
            binding.dialogET.filters = filterLength
            binding.dialogTIL.counterMaxLength = maxLength
            binding.dialogTIL.isCounterEnabled = true

        }
        binding.dialogOK.isVisible = dialogState.ok
        binding.dialogCancel.isVisible = dialogState.cancel

        Log.d(TAG, "Created")

        binding.dialogCancel.setOnClickListener {
            close()
        }

        binding.dialogOK.setOnClickListener {
            emitResult(binding.dialogET.text.toString())
            close()
        }
    }

    private fun emitResult(text: String) {
        parentFragmentManager.setFragmentResult(
            requestKey, bundleOf(
                RESPONSE_TAG to text
            )
        )
    }

    private fun close() {
        parentFragmentManager.beginTransaction().remove(this).commit()
    }

    companion object {
        private val TAG = MultiChooseDialog::class.java.simpleName
        private const val BUNDLE_TAG = "dialogState"
        private const val RESPONSE_TAG = "RESPONSE"
        private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"

        fun show(
            manager: FragmentManager,
            dialogState: EditTextDialogStates,
            requestKey: String
        ) {
            val dialogFragment = EditTextDialog()
            dialogFragment.arguments =
                bundleOf(
                    BUNDLE_TAG to dialogState,
                    ARG_REQUEST_KEY to requestKey
                )
            dialogFragment.show(manager, TAG)
        }

        fun setupListener(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            requestKey: String,
            listener: CustomEditTextDialogListener
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
