package space.active.taskmanager1c.presentation.screens.task_attachments

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import space.active.taskmanager1c.R
import space.active.taskmanager1c.data.local.cache_storage.models.CachedFile
import space.active.taskmanager1c.databinding.AttachmentItemBinding

class AttachmentsAdapter(
    private val clickViews: ClickViews
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
            clickViews.onLongClick(it, storageItem)
        }
        return true
    }

    inner class AttachmentsViewHolder(val itemBind: AttachmentItemBinding) :
        RecyclerView.ViewHolder(itemBind.root) {

        fun renderLoadingState(item: CachedFile) {
            if (item.loading) {
                itemBind.apply {
                    groupLoadingProgress.isVisible = true
                    progressAttach.startAnimation(
                        AnimationUtils.loadAnimation(
                            root.context,
                            R.anim.clockwise_rotation_infinite
                        )
                    )
                    imageViewItem.visibility = View.INVISIBLE
                }
            } else {
                itemBind.apply {
                    groupNotUploaded.isVisible = item.notUploaded
                    groupLoadingProgress.isVisible = false
                    progressAttach.clearAnimation()
                    imageViewItem.visibility = View.VISIBLE
                }
            }
        }

        fun renderLoadingProgress(item: CachedFile) {
            if (!itemBind.groupLoadingProgress.isVisible) renderLoadingState(item)
            val progress = item.progress
            itemBind.progressTV.text = progress.toString()
            itemBind.progressAttach.progress = progress
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AttachmentItemBinding.inflate(inflater, parent, false)
        binding.root.setOnClickListener(this)
        binding.root.setOnLongClickListener(this)
        return AttachmentsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AttachmentsViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads[0] is AttachmentPayloads) {
            val attachmentPayloads = payloads[0] as AttachmentPayloads
            val item = currentList[position]
            if (attachmentPayloads.loadingChange){
                holder.renderLoadingState(item)
            }
            if (attachmentPayloads.progressChange) {
                holder.renderLoadingProgress(item)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: AttachmentsViewHolder, position: Int) {
        val item = currentList[position]
        holder.itemView.tag = item
        holder.itemBind.apply {
            Log.d("AttachmentsAdapter", "item ${item}")
            if (!item.loading) {
                groupLoadingProgress.isVisible = false
                imageViewItem.visibility = View.VISIBLE
            }
            imageViewItem.cachedState(item.cached, item, item.notUploaded)
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
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
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

        override fun getChangePayload(oldItem: CachedFile, newItem: CachedFile): Any? {
            val loadingChange = newItem.loading && oldItem.loading != newItem.loading
            val progressChange = newItem.progress > 0 && oldItem.progress != newItem.progress
            return if (loadingChange || progressChange) AttachmentPayloads(
                loadingChange,
                progressChange
            ) else null
        }
    }
}

