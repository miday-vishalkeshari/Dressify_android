package com.example.dressify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dressify.adapters.BigImageAdapter
import com.example.dressify.adapters.MediumImageAdapter
import com.example.dressify.models.ImageItem
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.toString

class ImageDetailActivity : AppCompatActivity(), BigImageAdapter.OnItemActionListener {

    private lateinit var fullImageRecyclerView: RecyclerView
    private lateinit var matchingRecyclerView: RecyclerView
    private lateinit var imageTitle: TextView
    private lateinit var imageDescription: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var collectionName: String
    private lateinit var styleColour: String
    private lateinit var docId: String
    private lateinit var userdocumentId: String
    private var productLink: String? = null

    private val wishlistChanges = mutableListOf<Map<String, String>>()
    private val wishlistRemovals = mutableListOf<Map<String, String>>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        initializeViews()
        initializeFirestore()

        val clickedImageUrl = intent.getStringExtra("imageUrl")
        collectionName = intent.getStringExtra("styleType") ?: "tshirts"
        styleColour = intent.getStringExtra("styleColour").toString()
        docId = intent.getStringExtra("productDocId").toString()
        userdocumentId = intent.getStringExtra("userdocumentId").toString()

        setupFullImageRecyclerView(clickedImageUrl, docId, collectionName)//abhi ke liye esko farak nhi padta
        fetchImageDetails(collectionName,styleColour,docId)///done
        setHardcodedDescription()
        fetchMatchingImages(collectionName)
    }

    private fun initializeViews() {
        fullImageRecyclerView = findViewById(R.id.fullImageRecyclerView)
        matchingRecyclerView = findViewById(R.id.matchingRecyclerView)
        imageTitle = findViewById(R.id.imageTitle)
        imageDescription = findViewById(R.id.imageDescription)




//        val imageItemList = listOf("image_url_1", "image_url_2") // Example data
//        val adapter = BigImageAdapter(this, imageItemList, "docId", "collectionName", this)
//        val recyclerView: RecyclerView = findViewById(R.id.fullImageRecyclerView)
//        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
//        recyclerView.adapter = adapter

    }

    private fun initializeFirestore() {
        db = FirebaseFirestore.getInstance()
    }

    override fun onAddToWishlist(docId: String, collectionName: String, isAdded: Boolean) {
        val wishlistItem = mapOf(
            "dress_type" to collectionName,
            "cloth_item" to docId
        )

        if (isAdded) {
            wishlistChanges.add(wishlistItem)
            wishlistRemovals.remove(wishlistItem) // Ensure no duplicate removals
        } else {
            wishlistRemovals.add(wishlistItem)
            wishlistChanges.remove(wishlistItem) // Ensure no duplicate additions
        }
    }

    override fun onPause() {
        super.onPause()
        updateWishlistOnFirestore()
    }

    private fun updateWishlistOnFirestore() {
        var userDocRef: DocumentReference = db.collection("Dressify_users").document(userdocumentId)

        if (wishlistChanges.isNotEmpty() || wishlistRemovals.isNotEmpty()) {
            val updates = mutableMapOf<String, Any>()

            if (wishlistChanges.isNotEmpty()) {
                updates["wishlist"] = FieldValue.arrayUnion(*wishlistChanges.toTypedArray())
            }
            if (wishlistRemovals.isNotEmpty()) {
                updates["wishlist"] = FieldValue.arrayRemove(*wishlistRemovals.toTypedArray())
            }

            userDocRef.update(updates)
                .addOnSuccessListener {
                    Log.d("ImageDetailActivity", "Wishlist updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("ImageDetailActivity", "Error updating wishlist: ${exception.message}")
                }

            // Clear local changes after update
            wishlistChanges.clear()
            wishlistRemovals.clear()
        }
    }

    override fun onLinkClicked(docId: String, collectionName: String) {
        productLink?.let { link ->
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(link)
            }
            startActivity(browserIntent)
        } ?: Toast.makeText(this, "No link available to open", Toast.LENGTH_SHORT).show()
    }



    private fun setupFullImageRecyclerView(imageUrl: String?, docId: String?, collectionName: String) {
        val fullImageList = arrayListOf<String>().apply { imageUrl?.let { add(it) } }
        fullImageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        fullImageRecyclerView.adapter = BigImageAdapter(this, fullImageList, docId.orEmpty(), collectionName, this)
    }



    private fun fetchImageDetails(styleType: String,styleColour: String?, productDocId: String?) {
        if (productDocId == null) {
            showToast("Invalid image reference")
            return
        }

        db.collection("Dressify_styles").document(styleType).collection(styleColour.toString()).document(
            productDocId.toString()
        ).get()
            .addOnSuccessListener { document ->
                document?.let {
                    imageTitle.text = it.getString("brand") ?: "No Title"

                    // Store the link value in the global variable
                    productLink = it.getString("link")
                }
            }
            .addOnFailureListener { exception ->
                logError("Error fetching image details", exception)
                showToast("Error fetching image details: ${exception.message}")
            }
    }

    @SuppressLint("SetTextI18n")
    private fun setHardcodedDescription() {
        imageDescription.text = """
            This stylish pair of jeans is perfect for casual outings or a day at the park.
            Made from high-quality denim, it features a slim fit with a classic five-pocket design.
            The rich blue color adds versatility, making it easy to pair with a variety of tops, from t-shirts to blouses.
            Whether you're going for a laid-back look or dressing it up with a blazer, these jeans are a wardrobe staple.
        """.trimIndent()
    }

    private fun fetchMatchingImages(collectionName: String) {
        val oppositeCollection = getOppositeCollection(collectionName) ?: return
        val matchingList = mutableListOf<ImageItem>()


        db.collection("Dressify_styles").document(collectionName).collection(styleColour.toString()).get()
            .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    val imageUrl = (document.get("image_urls") as? List<*>)?.getOrNull(0) as? String
                    imageUrl?.let {
                        matchingList.add(ImageItem(it, oppositeCollection,styleColour, document.id))
                    }
                }
                setupMatchingAdapter(matchingList)
            }
            .addOnFailureListener { exception ->
                logError("Error getting documents", exception)
                showToast("Error getting documents: ${exception.message}")
            }
    }

    private fun getOppositeCollection(collectionName: String): String? {
        return when (collectionName) {
            "pants" -> "formal_shirts"
            "tshirts" -> "jeans"
            "jeans" -> "tshirts"
            "casual_shirts" -> "track_pants"
            "track_pants" -> "casual_shirts"
            "formal_shirts" -> "trousers"
            "trousers" -> "formal_shirts"
            "shorts" -> "casual_shirts"
            else -> {
                logWarning("Unknown collection name: $collectionName")
                null
            }
        }
    }

    private fun setupMatchingAdapter(matchingItems: List<ImageItem>) {
        matchingRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        matchingRecyclerView.adapter = MediumImageAdapter(
            this,
            matchingItems,
            "ImageDetailActivity",
            userdocumentId
        ) { itemToDelete ->
            // Handle delete action
//            db.collection(itemToDelete.collectionName)
//                .document(itemToDelete.documentId)
//                .delete()
//                .addOnSuccessListener {
//                    // Remove the item from the list and notify the adapter
//                    (matchingItems as MutableList).remove(itemToDelete)
//                    matchingRecyclerView.adapter?.notifyDataSetChanged()
//                    Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { exception ->
//                    Toast.makeText(this, "Error deleting item: ${exception.message}", Toast.LENGTH_SHORT).show()
//                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun logError(message: String, exception: Exception) {
        Log.e("ImageDetailActivity", "$message: ${exception.message}", exception)
    }

    private fun logWarning(message: String) {
        Log.w("ImageDetailActivity", message)
    }
}