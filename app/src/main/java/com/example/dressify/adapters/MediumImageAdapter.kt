package com.example.dressify.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dressify.ImageDetailActivity
import com.example.dressify.models.ImageItem
import com.example.dressify.R

class MediumImageAdapter(
    private val context: Context,
    private val imageItemList: List<ImageItem>,
    private val activityType: String,
    private val userdocumentId: String
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



        holder.itemView.setOnClickListener {
            val intent = Intent(context, ImageDetailActivity::class.java)
            intent.putExtra("imageList", currentItem.imageUrl)
            intent.putExtra("collectionName", currentItem.collectionName)
            intent.putExtra("docId", currentItem.documentId)
            intent.putExtra("userdocumentId", userdocumentId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = imageItemList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}