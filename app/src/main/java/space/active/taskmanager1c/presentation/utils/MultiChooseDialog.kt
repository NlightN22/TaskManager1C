package space.active.taskmanager1c.presentation.utils


import android.os.Bundle
import android.os.Parcelable
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
import kotlinx.parcelize.Parcelize
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.DialogSingleMultiChooseBinding
import space.active.taskmanager1c.databinding.ListItemBinding
import space.active.taskmanager1c.presentation.utils.MultiChooseDialog.DialogItem.Companion.toggleDialogItem

class MultiChooseDialog : DialogFragment(R.layout.dialog_single_multi_choose) {

    lateinit var binding: DialogSingleMultiChooseBinding
    lateinit var dialogItems: List<DialogItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DialogSingleMultiChooseBinding.bind(view)

        dialogItems = if (savedInstanceState == null) {
            requireArguments().getParcelableArrayList<DialogItem>(PARCE_TAG)
                ?: listOf<DialogItem>()
        } else {
            savedInstanceState.getParcelableArrayList<DialogItem>(
                PARCE_TAG
            ) ?: listOf()
        }

        val ok: Boolean = requireArguments().getBoolean(OK_TAG)
        val cancel: Boolean = requireArguments().getBoolean(CANCEL_TAG)

        binding.dialogOK.isVisible = ok
        binding.dialogCancel.isVisible = cancel

        Log.d("MultiChooseDialog", "$dialogItems")

        val itemsAdapter = DialogAdapter(object : DialogListener {
            override fun onClickItem(filteredList: List<DialogItem>) {
                Log.d("MultiChooseDialog", " filteredList $filteredList")
                val filteredId = filteredList.map { it.id }
                dialogItems = dialogItems.map { dialogitem ->
                    if (filteredId.contains(dialogitem.id)) {
                        dialogitem.copy(checked = true)
                    } else {
                        dialogitem.copy(checked = false)
                    }
                }
                Log.d("MultiChooseDialog", "dialogItems $dialogItems")
            }
        })

        binding.dialogCancel.setOnClickListener {
            close()
        }

        binding.dialogOK.setOnClickListener {
            emitResult(dialogItems)
        }

        binding.listItemRV.adapter = itemsAdapter
        itemsAdapter.listItems = dialogItems

        binding.searchDialog.addTextChangedListener { editable ->
            editable?.let { textChar ->
                val filteredList = dialogItems.filter { it.text.contains(textChar, true) }
                itemsAdapter.listItems = filteredList
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(
            PARCE_TAG,
            dialogItems as java.util.ArrayList<out Parcelable>
        )
    }

    private fun emitResult(selectedItems: List<DialogItem>) {
        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            bundleOf(RESPONSE_TAG to selectedItems)
        )
    }

    private fun close() {
        parentFragmentManager.beginTransaction().remove(this).commit()
    }

    interface DialogListener {
        fun onClickItem(filteredList: List<DialogItem>)
    }

    class DialogAdapter(val listener: DialogListener) :
        RecyclerView.Adapter<DialogAdapter.ItemViewHolder>() {


        var listItems: List<DialogItem> = emptyList()
            set(newValue) {
                field = newValue
                notifyDataSetChanged()
            }

        fun onClickItem(item: DialogItem) {
            // change state
            listItems = listItems.toggleDialogItem(item)
            // send to if
            listener.onClickItem(listItems.filter { it.checked })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding: ListItemBinding = ListItemBinding.inflate(inflater, parent, false)
            return ItemViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val item = listItems[position]
            with(holder.itemBind) {
                listCardView.isChecked = item.checked
                listTextView.text = item.text
                listCardView.setOnClickListener {
                    onClickItem(item)
                    notifyItemChanged(position)
                }
            }
        }

        override fun getItemCount(): Int = listItems.size

        class ItemViewHolder(val itemBind: ListItemBinding) :
            RecyclerView.ViewHolder(itemBind.root) {
        }

    }

    @Parcelize
    data class DialogItem(
        val id: String,
        val text: String,
        var checked: Boolean
    ) : Parcelable {
        companion object {
            fun List<DialogItem>.toggleDialogItem(item: DialogItem): List<DialogItem> {
                return this.map {
                    if (it.id == item.id) {
                        it.copy(checked = !item.checked)
                    } else {
                        it
                    }
                }
            }
        }
    }

    companion object {
        private const val OK_TAG = "OK"
        private const val CANCEL_TAG = "CANCEL"
        private const val PARCE_TAG = "DIALOG_ITEMS"

        @JvmStatic
        private val TAG = MultiChooseDialog::class.java.simpleName

        @JvmStatic
        private val RESPONSE_TAG = "RESPONSE"

        @JvmStatic
        val REQUEST_KEY = "$TAG:defaultRequestKey"

        fun show(
            manager: FragmentManager,
            listItems: List<DialogItem>,
            ok: Boolean = true,
            cancel: Boolean = true
        ) {
            val dialogFragment = MultiChooseDialog()
            dialogFragment.arguments =
                bundleOf(PARCE_TAG to listItems, OK_TAG to ok, CANCEL_TAG to cancel)
            dialogFragment.show(manager, TAG)
        }

        fun setupListener(
            manager: FragmentManager,
            lifecycleOwner: LifecycleOwner,
            listener: (ArrayList<String>?) -> Unit
        ) {
            manager.setFragmentResultListener(
                REQUEST_KEY,
                lifecycleOwner
            ) { _, result ->
                listener.invoke(result.getStringArrayList(RESPONSE_TAG))
            }
        }
    }

}
