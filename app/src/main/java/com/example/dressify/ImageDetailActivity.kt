package com.example.dressify

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ImageDetailActivity : AppCompatActivity() {

    private lateinit var fullImageRecyclerView: RecyclerView
    private lateinit var matchingRecyclerView: RecyclerView
    private lateinit var imageTitle: TextView
    private lateinit var imageDescription: TextView
    private lateinit var imageSource: TextView
    private lateinit var db: FirebaseFirestore

    private lateinit var oppositeCollection: String

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_detail)

        fullImageRecyclerView = findViewById(R.id.fullImageRecyclerView)
        matchingRecyclerView = findViewById(R.id.matchingRecyclerView)

        imageTitle = findViewById(R.id.imageTitle)
        imageDescription = findViewById(R.id.imageDescription)
        imageSource = findViewById(R.id.imageSource)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Get clicked image URL, collectionName, and docId from intent
        val clickedImageUrl = intent.getStringExtra("imageList")
        val collectionName = intent.getStringExtra("collectionName")
        val docId = intent.getStringExtra("docId")

        // Set up Full Image RecyclerView
        val fullImageList = arrayListOf<String>()
        clickedImageUrl?.let {
            fullImageList.add(it)
        }

        fullImageRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        fullImageRecyclerView.adapter = BigImageAdapter(this, fullImageList,
            docId.toString(), collectionName.toString()
        )

        // Fetch and show image details using docId
        if (docId != null) {
            fetchImageDetails(docId, collectionName ?: "tshirts")  // Default to "pants" if collectionName is null
        } else {
            Toast.makeText(this, "Invalid image reference", Toast.LENGTH_SHORT).show()
        }

        // Set hardcoded description
        imageDescription.text = """
            This stylish pair of jeans is perfect for casual outings or a day at the park. 
            Made from high-quality denim, it features a slim fit with a classic five-pocket design. 
            The rich blue color adds versatility, making it easy to pair with a variety of tops, from t-shirts to blouses. 
            Whether you're going for a laid-back look or dressing it up with a blazer, these jeans are a wardrobe staple.
        """.trimIndent()

        // Fetch matching images
        fetchMatchingImages(collectionName.toString())
    }

    private fun fetchImageDetails(docId: String, collectionName: String) {
        db.collection(collectionName)  // Use dynamic collection name
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val title = document.getString("brand")
                    val sourceLink = document.getString("link")

                    // Debugging: Log the fetched details
                    Log.d("ImageDetailActivity", "Fetched document: $title, $sourceLink")

                    imageTitle.text = title ?: "No Title"
                    imageSource.text = "Source: ${sourceLink ?: "N/A"}"
                }
            }
            .addOnFailureListener { exception ->
                // Debugging: Log error message
                Log.e("ImageDetailActivity", "Error fetching image details: ${exception.message}")
                Toast.makeText(this, "Error fetching image details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchMatchingImages(collectionName: String) {
        val matchingList = mutableListOf<ImageItem>()

        // Determine the opposite collection to fetch based on predefined wearable pairs
        val oppositeCollection = when (collectionName) {
            "pants" -> "shirts"
            "shirts" -> "pants"
            "tshirts" -> "jeans"
            "jeans" -> "tshirts"
            "casual_shirts" -> "track_pants"
            "track_pants" -> "casual_shirts"
            "formal_shirts" -> "trousers"
            "trousers" -> "formal_shirts"
            "shorts" -> "casual_shirts"
            else -> {
                Log.w("ImageDetailActivity", "Unknown collection name: $collectionName")
                return
            }
        }

        // Fetch images from the opposite collection
        db.collection(oppositeCollection)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrls = document.get("image_urls") as? List<*>
                    val imageUrl = imageUrls?.getOrNull(0) as? String
                    val documentId = document.id

                    if (imageUrl != null) {
                        matchingList.add(ImageItem(imageUrl, oppositeCollection, documentId))
                    }
                }

                Log.d("ImageDetailActivity", "Found ${matchingList.size} matching images from $oppositeCollection")

                // Pass list of ImageItem directly
                setupMatchingAdapter(matchingList)
            }
            .addOnFailureListener { exception ->
                Log.e("ImageDetailActivity", "Error getting documents: ${exception.message}")
                Toast.makeText(this, "Error getting documents: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupMatchingAdapter(matchingItems: List<ImageItem>) {
        Log.d("ImageDetailActivity", "Setting up matching adapter with ${matchingItems.size} items")

        matchingRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        matchingRecyclerView.adapter = MediumImageAdapter(this, matchingItems)
    }

}
