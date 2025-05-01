package com.example.dressify

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide



class MediumImageAdapter(
    private val context: Context,
    private val imageItemList: List<ImageItem>
) : RecyclerView.Adapter<MediumImageAdapter.ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.medium_item_image, parent, false)
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
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = imageItemList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
