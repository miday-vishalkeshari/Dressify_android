package com.example.dressify

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dressify.adapters.MediumImageAdapter
import com.example.dressify.models.ImageItem
import com.google.firebase.firestore.FirebaseFirestore

class WishlistActivity : AppCompatActivity() {

    private var documentId: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Fetch the documentId from the Intent
        documentId = intent.getStringExtra("documentId")
        Log.d("WishlistActivity", "Received documentId: $documentId")

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columns in grid layout

        // Fetch wishlist data
        fetchWishlist()
    }

    private fun fetchWishlist() {
        if (documentId == null) {
            Log.e("WishlistActivity", "Document ID is null")
            return
        }

        db.collection("Dressify_users").document(documentId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val wishlist = document.get("wishlist") as? List<Map<String, String>>
                    if (wishlist != null) {
                        val imageItemList = mutableListOf<ImageItem>()

                        for (item in wishlist) {
                            val dressType = item["dress_type"]
                            val clothItem = item["cloth_item"]

                            if (dressType != null && clothItem != null) {
                                db.collection(dressType).document(clothItem)
                                    .get()
                                    .addOnSuccessListener { clothDocument ->
                                        if (clothDocument.exists()) {
                                            val imageUrls = clothDocument.get("image_urls") as? List<String>
                                            val firstImageUrl = imageUrls?.getOrNull(0)

                                            if (firstImageUrl != null) {
                                                imageItemList.add(ImageItem(firstImageUrl, dressType, clothItem))
                                                setupRecyclerView(imageItemList)
                                            } else {
                                                Log.e("WishlistActivity", "No image URLs found in $clothItem")
                                            }
                                        } else {
                                            Log.e("WishlistActivity", "Document $clothItem does not exist in $dressType")
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e("WishlistActivity", "Error fetching document: ${exception.message}", exception)
                                    }
                            } else {
                                Log.e("WishlistActivity", "Invalid wishlist item: $item")
                            }
                        }
                    } else {
                        Log.e("WishlistActivity", "Wishlist is null or empty")
                    }
                } else {
                    Log.e("WishlistActivity", "Document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("WishlistActivity", "Error fetching wishlist: ${exception.message}", exception)
            }
    }

    private fun setupRecyclerView(imageItemList: List<ImageItem>) {
        recyclerView.adapter = MediumImageAdapter(this, imageItemList, "WishlistActivity")
    }
}