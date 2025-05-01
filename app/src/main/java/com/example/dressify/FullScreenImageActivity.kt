package com.example.dressify

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        // Get the docId and collectionName from the Intent
        val docId = intent.getStringExtra("docId")
        val collectionName = intent.getStringExtra("collectionName")

        // Display docId and collectionName for debugging purposes (if needed)
        Log.d("FullScreenImageActivity", "docId: $docId, collectionName: $collectionName")


        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView for displaying images
        val fullScreenRecyclerView = findViewById<RecyclerView>(R.id.fullScreenRecyclerView)

        // Fetch image URLs from Firestore
        if (docId != null && collectionName != null) {
            fetchImageUrls(collectionName, docId, fullScreenRecyclerView)
        } else {
            Toast.makeText(this, "Invalid data received", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchImageUrls(collectionName: String, docId: String, recyclerView: RecyclerView) {
        // Reference to the document in Firestore
        val docRef = db.collection(collectionName).document(docId)

        // Fetch document data
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Retrieve image_urls from the document
                    val imageUrls = documentSnapshot.get("image_urls") as? List<String> ?: emptyList()

                    // Set up RecyclerView with the image URLs
                    recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                    recyclerView.adapter = FullImageAdapter(this, imageUrls, docId, collectionName)

                } else {
                    Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting document: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
