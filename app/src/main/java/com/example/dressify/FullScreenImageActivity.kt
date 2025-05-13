package com.example.dressify

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dressify.adapters.FullImageAdapter
import com.google.firebase.firestore.FirebaseFirestore

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var fullScreenRecyclerView: RecyclerView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        initializeComponents()
        fetchAndDisplayImages()
    }

    private fun initializeComponents() {
        db = FirebaseFirestore.getInstance()
        fullScreenRecyclerView = findViewById(R.id.fullScreenRecyclerView)
        fullScreenRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
    }

    private fun fetchAndDisplayImages() {
        val styleType = intent.getStringExtra("styleType")
        val styleColour = intent.getStringExtra("styleColour")
        val productDocId = intent.getStringExtra("productDocId")



        if (productDocId.isNullOrEmpty() || styleType.isNullOrEmpty()|| styleColour.isNullOrEmpty()) {
            showToast("Invalid data received")
            return
        }

        Log.d("FullScreenImageActivity", "styleType: $styleType, styleColour: $styleColour, productDocId: $productDocId")
        fetchImageUrls(styleType.toString(), styleColour.toString(), productDocId.toString())
    }

    private fun fetchImageUrls(styleType: String,styleColour: String, productDocId: String) {
        db.collection("Dressify_styles").document(styleType).collection(styleColour).document(productDocId).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val imageUrls = documentSnapshot.get("image_urls") as? List<String> ?: emptyList()
                    setupAdapter(imageUrls)
                } else {
                    showToast("Document not found")
                }
            }
            .addOnFailureListener { exception ->
                showToast("Error getting document: ${exception.message}")
            }
    }

    private fun setupAdapter(imageUrls: List<String>) {
        fullScreenRecyclerView.adapter = FullImageAdapter(this, imageUrls)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}