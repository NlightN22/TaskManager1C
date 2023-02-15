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
import jp.wasabeef.picasso.transformations.BlurTransformation
import space.active.taskmanager1c.R
import space.active.taskmanager1c.databinding.AttachmentItemBinding
import space.active.taskmanager1c.data.local.cache_storage.models.CachedFile

class AttachmentsAdapter(
    private val clickViews: AttachmentsAdapter.ClickViews
) :
    ListAdapter<CachedFile, AttachmentsAdapter.AttachmentsViewHolder>(Companion),
    View.OnClickListener, View.OnLongClickListener {

    interface ClickViews {
        fun onItemClick(view: View, item: CachedFile)
        fun onOptionsMenuClick(view: View, item: CachedFile)
        fun onLongClick(view: View, item: CachedFile)
    }

    override fun onClick(v: View?) {
        v?.let {
            val storageItem = it.tag as CachedFile
            clickViews.onItemClick(it, storageItem)
        }
    }

    override fun onLongClick(v: View?): Boolean {
        v?.let {
            val storageItem = it.tag as CachedFile
            clickViews.onLongClick(it,storageItem)
        }
        return true
    }

    inner class AttachmentsViewHolder(val itemBind: AttachmentItemBinding) :
        RecyclerView.ViewHolder(itemBind.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AttachmentItemBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener(this)
        binding.root.setOnLongClickListener(this)
        return AttachmentsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
        val item = currentList[position]
        holder.itemView.tag = item
        holder.itemBind.apply {
            progressAttach.isVisible = item.loading
            imageViewItem.visibility = if (item.loading) {View.INVISIBLE} else {View.VISIBLE}
            imageViewItem.cachedState(item.cached, item, item.notUploaded)
            groupNotUploaded.isVisible = item.notUploaded && !item.loading
            fileName.text = item.filename
            attachmentOptions.setOnClickListener { clickViews.onOptionsMenuClick(it, item) }
        }
    }

    private fun ImageView.cachedState(
        cached: Boolean,
        item: CachedFile,
        notUploaded: Boolean
    ) {
        if (cached) {
            try {
                item.uri?.let { fileUri ->
                    val picassoPreview = Picasso.get()
                        .load(fileUri)
                        .fit()
                        .placeholder(R.drawable.ic_baseline_cloud_24)
                        .error(R.drawable.ic_baseline_image_not_supported_24)
                    if (notUploaded) {
                        picassoPreview.transform(BlurTransformation(this.context, 25, 1))
                    }
                    picassoPreview.into(this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            this.setImageResource(R.drawable.ic_baseline_cloud_24)
        }
    }


    companion object : DiffUtil.ItemCallback<CachedFile>() {
        override fun areItemsTheSame(
            oldItem: CachedFile,
            newItem: CachedFile
        ): Boolean {
            return newItem.id == oldItem.id
        }

        override fun areContentsTheSame(
            oldItem: CachedFile,
            newItem: CachedFile
        ): Boolean {
            return newItem == oldItem
        }
    }
}

