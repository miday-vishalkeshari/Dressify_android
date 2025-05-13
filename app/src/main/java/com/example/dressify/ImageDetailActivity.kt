package com.example.dressify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var styleType: String
    private lateinit var styleColour: String
    private lateinit var productDocId: String
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
        styleType = intent.getStringExtra("styleType") ?: "tshirts"
        styleColour = intent.getStringExtra("styleColour").toString()
        productDocId = intent.getStringExtra("productDocId").toString()
        userdocumentId = intent.getStringExtra("userdocumentId").toString()

        setupFullImageRecyclerView(clickedImageUrl, productDocId, styleType)//abhi ke liye esko farak nhi padta
        fetchImageDetails(styleType,styleColour,productDocId)///done
        setHardcodedDescription()
        fetchMatchingImages(styleType)


        val titleSection = findViewById<LinearLayout>(R.id.titleSection)
        val imageDescription = findViewById<TextView>(R.id.imageDescription)

        titleSection.setOnClickListener {
            if (imageDescription.visibility == View.GONE) {
                imageDescription.visibility = View.VISIBLE
            } else {
                imageDescription.visibility = View.GONE
            }
        }
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

    override fun onAddToWishlist(styleType: String, styleColour: String, productDocId: String, isAdded: Boolean) {
        val wishlistItem = mapOf(
            "styleType" to styleType,
            "styleColour" to styleColour,
            "productDocId" to productDocId
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

    override fun onLinkClicked(styleType: String,styleColour: String,productDocId: String) {
        productLink?.let { link ->
            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(link)
            }
            startActivity(browserIntent)
        } ?: Toast.makeText(this, "No link available to open", Toast.LENGTH_SHORT).show()
    }



    private fun setupFullImageRecyclerView(imageUrl: String?, docId: String?, styleType: String) {
        val fullImageList = arrayListOf<String>().apply { imageUrl?.let { add(it) } }
        fullImageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        fullImageRecyclerView.adapter = BigImageAdapter(this, fullImageList, styleType, styleColour,productDocId, this)
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

    private fun fetchMatchingImages(styleType: String) {
        val oppositeCollection = getOppositeCollection(styleType) ?: "tshirts"
        val matchingList = mutableListOf<ImageItem>()

        Log.d("fetchMatchingImages after ", "$oppositeCollection ")
        Log.d("fetchMatchingImages colour ", "$styleColour ")


        db.collection("Dressify_styles").document(oppositeCollection).collection(styleColour.toString()).get()// here if we have black colour then we wont get any tshirt kyu ki black tshirt abhi nhi hai hmare pas db par
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

    private fun getOppositeCollection(styleType: String): String? {
        return when (styleType) {
            "tshirts" -> "jeans"
            "jeans" -> "tshirts"
            "casual_shirts" -> "track_pants"
            "track_pants" -> "casual_shirts"
            "formal_shirts" -> "trousers"
            "trousers" -> "formal_shirts"
            "shorts" -> "casual_shirts"
            else -> {
                logWarning("Unknown collection name: $styleType")
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
            db.collection("Dressify_styles").document(itemToDelete.styleType).collection(itemToDelete.styleColour)
                .document(itemToDelete.productDocId)
                .delete()
                .addOnSuccessListener {
                    // Remove the item from the list and notify the adapter
                    (matchingItems as MutableList).remove(itemToDelete)
                    matchingRecyclerView.adapter?.notifyDataSetChanged()
                    Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error deleting item: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
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