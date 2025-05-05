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
    private val collectionName: String
) : RecyclerView.Adapter<BigImageAdapter.ImageViewHolder>() {

    private var isAddedToWishlist = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.big_image_item, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentItem = imageItemList[position]
        Glide.with(context)
            .load(currentItem)
            .into(holder.imageView)

        // Handle click on the main image
        holder.imageView.setOnClickListener {
            val intent = Intent(context, FullScreenImageActivity::class.java)
            intent.putExtra("image_url", currentItem)
            intent.putExtra("docId", docId)
            intent.putExtra("collectionName", collectionName)
            context.startActivity(intent)
        }



        holder.addToWishlistIcon.setOnClickListener {
            if (isAddedToWishlist) {
                // Action for removing from wishlist
                holder.addToWishlistIcon.setImageResource(R.drawable.ic_unliked) // Replace with your "remove" icon
                Toast.makeText(context, "Removed from Wishlist", Toast.LENGTH_SHORT).show()
            } else {
                // Action for adding to wishlist
                holder.addToWishlistIcon.setImageResource(R.drawable.ic_liked) // Replace with your "add" icon
                Toast.makeText(context, "Added to Wishlist", Toast.LENGTH_SHORT).show()
            }
            isAddedToWishlist = !isAddedToWishlist // Toggle the state
        }


        // Handle click on the "Link" icon
        holder.linkIcon.setOnClickListener {
            Toast.makeText(context, "Link clicked", Toast.LENGTH_SHORT).show()
            // Add your logic for handling the link click here
        }
    }

    override fun getItemCount(): Int = imageItemList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.fullImageView)
        val addToWishlistIcon: ImageView = itemView.findViewById(R.id.addToWishlistIcon)
        val linkIcon: ImageView = itemView.findViewById(R.id.linkIcon)
    }
}