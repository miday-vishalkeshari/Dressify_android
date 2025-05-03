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
import com.example.dressify.R

class BigImageAdapter(
    private val context: Context,
    private val imageItemList: List<String>,
    private val docId: String,                // added this
    private val collectionName: String        // optional — if you need it
) : RecyclerView.Adapter<BigImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.big_image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentItem = imageItemList[position]
        Glide.with(context)
            .load(currentItem)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            val intent = Intent(context, FullScreenImageActivity::class.java)
            intent.putExtra("image_url", currentItem)
            intent.putExtra("docId", docId)
            intent.putExtra("collectionName", collectionName)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = imageItemList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.fullImageView)
    }
}