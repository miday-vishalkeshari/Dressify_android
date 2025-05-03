package com.example.dressify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dressify.R

class FullImageAdapter(
    private val context: Context,
    private val imageItemList: List<String>,
    private val docId: String,                // added docId
    private val collectionName: String        // added collectionName
) : RecyclerView.Adapter<FullImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_full_screen_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentItem = imageItemList[position]

        // Load the image into the ImageView using Glide
        Glide.with(context)
            .load(currentItem)
            .into(holder.imageView)

    }

    override fun getItemCount(): Int = imageItemList.size

    // ViewHolder to hold the ImageView for each image
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.fullScreenImageView)
    }
}