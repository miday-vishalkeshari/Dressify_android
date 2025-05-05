package com.example.dressify.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dressify.FullScreenImageActivity
import com.example.dressify.R

class BigImageAdapter(
    private val context: Context,
    private val imageItemList: List<String>,
    private val docId: String,
    private val collectionName: String,
    private val listener: OnItemActionListener
) : RecyclerView.Adapter<BigImageAdapter.ImageViewHolder>() {

    private var isAddedToWishlist = false

    interface OnItemActionListener {
        fun onAddToWishlist(docId: String, collectionName: String, isAdded: Boolean)
        fun onLinkClicked(docId: String, collectionName: String)
    }

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

        holder.addToWishlistIcon.setOnClickListener {
            isAddedToWishlist = !isAddedToWishlist
            holder.addToWishlistIcon.setImageResource(
                if (isAddedToWishlist) R.drawable.ic_liked else R.drawable.ic_unliked
            )
            listener.onAddToWishlist(docId, collectionName, isAddedToWishlist)
        }

        holder.linkIcon.setOnClickListener {
            listener.onLinkClicked(docId, collectionName)
        }
    }

    override fun getItemCount(): Int = imageItemList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.fullImageView)
        val addToWishlistIcon: ImageView = itemView.findViewById(R.id.addToWishlistIcon)
        val linkIcon: ImageView = itemView.findViewById(R.id.linkIcon)
    }
}