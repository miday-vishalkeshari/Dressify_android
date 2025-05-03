package com.example.dressify

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dressify.adapters.BigImageAdapter
import com.example.dressify.adapters.MediumImageAdapter
import com.example.dressify.models.ImageItem
import com.google.firebase.firestore.FirebaseFirestore

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var fullImageRecyclerView: RecyclerView
    private lateinit var matchingRecyclerView: RecyclerView
    private lateinit var imageTitle: TextView
    private lateinit var imageDescription: TextView
    private lateinit var imageSource: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        initializeViews()
        initializeFirestore()

        val clickedImageUrl = intent.getStringExtra("imageList")
        val collectionName = intent.getStringExtra("collectionName") ?: "tshirts"
        val docId = intent.getStringExtra("docId")

        setupFullImageRecyclerView(clickedImageUrl, docId, collectionName)
        fetchImageDetails(docId, collectionName)
        setHardcodedDescription()
        fetchMatchingImages(collectionName)
    }

    private fun initializeViews() {
        fullImageRecyclerView = findViewById(R.id.fullImageRecyclerView)
        matchingRecyclerView = findViewById(R.id.matchingRecyclerView)
        imageTitle = findViewById(R.id.imageTitle)
        imageDescription = findViewById(R.id.imageDescription)
        imageSource = findViewById(R.id.imageSource)
    }

    private fun initializeFirestore() {
        db = FirebaseFirestore.getInstance()
    }

    private fun setupFullImageRecyclerView(imageUrl: String?, docId: String?, collectionName: String) {
        val fullImageList = arrayListOf<String>().apply { imageUrl?.let { add(it) } }
        fullImageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        fullImageRecyclerView.adapter = BigImageAdapter(this, fullImageList, docId.orEmpty(), collectionName)
    }

    private fun fetchImageDetails(docId: String?, collectionName: String) {
        if (docId == null) {
            showToast("Invalid image reference")
            return
        }

        db.collection(collectionName).document(docId).get()
            .addOnSuccessListener { document ->
                document?.let {
                    imageTitle.text = it.getString("brand") ?: "No Title"
                    imageSource.text = "Source: ${it.getString("link") ?: "N/A"}"
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

        db.collection(oppositeCollection).get()
            .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    val imageUrl = (document.get("image_urls") as? List<*>)?.getOrNull(0) as? String
                    imageUrl?.let {
                        matchingList.add(ImageItem(it, oppositeCollection, document.id))
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
        matchingRecyclerView.adapter = MediumImageAdapter(this, matchingItems, "MediumImageAdapter")
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