package com.example.dressify.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dressify.FullScreenImageActivity
import com.example.dressify.ImageDetailActivity
import com.example.dressify.models.ImageItem
import com.example.dressify.R

class MediumImageAdapter(
    private val context: Context,
    private val imageItemList: List<ImageItem>,
    private val activityType: String,//this is for name from where this is called
    private val userdocumentId: String,
    private val onDeleteClick: (ImageItem) -> Unit
) : RecyclerView.Adapter<MediumImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val layoutId = when (activityType) {
            "MainActivity" -> R.layout.medium_item_image_mainactivity // Layout for MainActivity
            "WishlistActivity" -> R.layout.medium_item_image_mainactivity // Layout for WishlistActivity
            else -> R.layout.medium_item_image_imagedetailactivity // Default layout
        }
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentItem = imageItemList[position]

        Glide.with(context)
            .load(currentItem.imageUrl)
            .into(holder.imageView)

        // Show or hide delete icon based on activityType
        if (activityType == "WishlistActivity") {
            holder.deleteIcon.visibility = View.VISIBLE
            holder.deleteIcon.setOnClickListener {
                onDeleteClick(currentItem) // Trigger callback
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, FullScreenImageActivity::class.java)
                intent.putExtra("docId", currentItem.documentId)
                intent.putExtra("collectionName", currentItem.collectionName)
                context.startActivity(intent)
            }
        } else {
            holder.deleteIcon.visibility = View.GONE
            holder.itemView.setOnClickListener {
                val intent = Intent(context, ImageDetailActivity::class.java)
                intent.putExtra("imageList", currentItem.imageUrl)
                intent.putExtra("collectionName", currentItem.collectionName)
                intent.putExtra("docId", currentItem.documentId)
                intent.putExtra("userdocumentId", userdocumentId)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = imageItemList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon) // Reference to delete icon
    }
}