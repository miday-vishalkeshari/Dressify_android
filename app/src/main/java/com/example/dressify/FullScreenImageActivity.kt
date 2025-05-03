package com.example.dressify

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var gestureDetector: GestureDetector

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        // Apply the enter transition
        //overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)

        // Get the docId and collectionName from the Intent
        val docId = intent.getStringExtra("docId")
        val collectionName = intent.getStringExtra("collectionName")

        Log.d("FullScreenImageActivity", "docId: $docId, collectionName: $collectionName")

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView for displaying images
        val fullScreenRecyclerView = findViewById<RecyclerView>(R.id.fullScreenRecyclerView)
        fullScreenRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        // Set up GestureDetector to detect upward swipes
//        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
//            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
//                if (e1 != null) {
//                    val deltaY = e2.y - e1.y
//                    if (deltaY < -500) { // Upward swipe
//                        finish() // Go back to the previous activity
//                        return true
//                    }
//                }
//                return false
//            }
//        })

//        // Attach touch listener to RecyclerView
//        fullScreenRecyclerView.setOnTouchListener { _, event ->
//            gestureDetector.onTouchEvent(event)
//        }

        // Fetch image URLs from Firestore
        if (docId != null && collectionName != null) {
            fetchImageUrls(collectionName, docId, fullScreenRecyclerView)
        } else {
            Toast.makeText(this, "Invalid data received", Toast.LENGTH_SHORT).show()
        }
    }


//    override fun finish() {
//        super.finish()
//        // Apply the exit transition
//        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down)
//    }

    private fun fetchImageUrls(collectionName: String, docId: String, recyclerView: RecyclerView) {
        val docRef = db.collection(collectionName).document(docId)

        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val imageUrls = documentSnapshot.get("image_urls") as? List<String> ?: emptyList()
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