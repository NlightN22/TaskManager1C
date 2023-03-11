package space.active.taskmanager1c.presentation.utils.dialogs


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.DialogSingleMultiChooseBinding
import space.active.taskmanager1c.databinding.ListItemBinding


typealias CustomSingleDialogListener = (requestKey: String, item: DialogItem?) -> Unit

class SingleChooseDialog : DialogFragment(R.layout.dialog_single_multi_choose) {

    private lateinit var binding: DialogSingleMultiChooseBinding

    lateinit var requestKey: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogSingleMultiChooseBinding.bind(view)

        requestKey = requireArguments().getString(SingleChooseDialog.ARG_REQUEST_KEY)!!

        val items: List<DialogItem> =
            arguments?.getParcelableArrayList<DialogItem>(BUNDLE_TAG) ?: listOf<DialogItem>()
        val ok: Boolean = arguments?.getBoolean(OK_TAG) ?: true
        val cancel: Boolean = arguments?.getBoolean(CANCEL_TAG) ?: true

        binding.dialogOK.isVisible = ok
        binding.dialogCancel.isVisible = cancel

        Log.d("SingleChooseDialog", "$items")

        val itemsAdapter = DialogAdapter(object : DialogListener {
            override fun onClickItem(item: DialogItem) {
                emitResult(item)
                close()
            }
        })

        binding.dialogCancel.setOnClickListener {
            close()
        }

        binding.listItemRV.adapter = itemsAdapter
        itemsAdapter.listItems = items

        binding.searchDialog.addTextChangedListener { editable ->
            editable?.let { textChar ->
                val filteredList = items.filter { it.text.contains(textChar, true) }
                itemsAdapter.listItems = filteredList
            }
        }
    }

    private fun emitResult(item: DialogItem) {
        parentFragmentManager.setFragmentResult(
            requestKey, bundleOf(
                RESPONSE_TAG to item
            )
        )
    }

    private fun close() {
        parentFragmentManager.beginTransaction().remove(this).commit()
    }

    interface DialogListener {
        fun onClickItem(item: DialogItem)
    }

    class DialogAdapter(val listener: DialogListener) :
        RecyclerView.Adapter<DialogAdapter.ItemViewHolder>() {


        var listItems: List<DialogItem> = emptyList()
            set(newValue) {
                field = newValue
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding: ListItemBinding = ListItemBinding.inflate(inflater, parent, false)
            return ItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = listItems[position]
            with(holder.itemBind) {
                listTextView.text = item.text
                listCardView.isChecked = item.checked
                listTextView.setOnClickListener {
                    listener.onClickItem(item)
                }
            }
        }

        override fun getItemCount(): Int = listItems.size

        class ItemViewHolder(val itemBind: ListItemBinding) :
            RecyclerView.ViewHolder(itemBind.root) {

        }

    }

    companion object {
        private const val OK_TAG = "OK"
        private const val CANCEL_TAG = "CANCEL"
        private const val ARG_REQUEST_KEY = "ARG_REQUEST_KEY"

        @JvmStatic
        private val TAG = SingleChooseDialog::class.java.simpleName

        @JvmStatic
        private val BUNDLE_TAG = "itemsList"

        @JvmStatic
        private val RESPONSE_TAG = "RESPONSE"

        fun show(
            manager: FragmentManager,
            listItems: List<DialogItem>,
            ok: Boolean = true,
            cancel: Boolean = true,
            requestKey: String
        ) {
            val dialogFragment = SingleChooseDialog()
            dialogFragment.arguments =
                bundleOf(
                    BUNDLE_TAG to listItems, OK_TAG to ok, CANCEL_TAG to cancel,
                    ARG_REQUEST_KEY to requestKey
                )
            dialogFragment.show(manager, TAG)
        }

        fun setupListener(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            requestKey: String,
            listener: CustomSingleDialogListener
        ) {
            manager.setFragmentResultListener(
                requestKey,
                lifecycleOwner
            ) { _, result ->
                listener.invoke(requestKey, result.getParcelable(RESPONSE_TAG))
            }
        }
    }

}
