package space.active.taskmanager1c.presentation.screens.task_attachments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.AttachmentItemBinding
import space.active.taskmanager1c.domain.models.InternalStorageFile

class AttachmentsAdapter(
    val onItemClick: (InternalStorageFile) -> Unit
) :
    ListAdapter<InternalStorageFile, AttachmentsAdapter.AttachmentsViewHolder>(Companion),
    View.OnClickListener {

    override fun onClick(v: View?) {
        v?.let {
            val storageItem = it.tag as InternalStorageFile
            onItemClick(storageItem)
        }
    }

    inner class AttachmentsViewHolder(val itemBind: AttachmentItemBinding) :
        RecyclerView.ViewHolder(itemBind.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AttachmentItemBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener(this)
        return AttachmentsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
        val item = currentList[position]
        holder.itemView.tag = item
        holder.itemBind.apply {
            imageViewItem.isVisible = !item.loading
            progressAttach.isVisible = item.loading
            imageViewItem.cachedState(item.cached, item)
            fileName.text = item.filename
        }
    }

    private fun ImageView.cachedState(state: Boolean, item: InternalStorageFile) {
        if (state) {
            try {
                item.uri?.let { fileUri ->
                    Picasso.get()
                        .load(fileUri)
                        .fit()
                        .placeholder(R.drawable.ic_baseline_cloud_24)
                        .error(R.drawable.ic_baseline_image_not_supported_24)
                        .into(this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            this.setImageResource(R.drawable.ic_baseline_cloud_24)
        }
    }

    companion object : DiffUtil.ItemCallback<InternalStorageFile>() {
        override fun areItemsTheSame(
            oldItem: InternalStorageFile,
            newItem: InternalStorageFile
        ): Boolean {
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(
            oldItem: InternalStorageFile,
            newItem: InternalStorageFile
        ): Boolean {
            return newItem.id == oldItem.id &&
                    newItem.cached == oldItem.cached &&
                    newItem.filename == oldItem.filename &&
                    newItem.uri == oldItem.uri
        }
    }
}

