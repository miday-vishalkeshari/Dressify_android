package com.example.dressify

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BodyTypeGalleryActivity : AppCompatActivity() {

    private lateinit var bodyTypeRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_body_type_gallery)

        bodyTypeRecyclerView = findViewById(R.id.bodyTypeRecyclerView)

        val bodyTypeImages = listOf(
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p3,
            R.drawable.p4,
            R.drawable.p5,
            R.drawable.p6,
            R.drawable.p7,
            R.drawable.p8,
            R.drawable.p9,
            R.drawable.p10,
            R.drawable.p11,
            R.drawable.p12,
            R.drawable.p13,
            R.drawable.p14,
            R.drawable.p15,
            R.drawable.p16
        )

        val adapter = BodyTypeAdapter(this, bodyTypeImages)
        bodyTypeRecyclerView.layoutManager = GridLayoutManager(this, 2)
        bodyTypeRecyclerView.adapter = adapter
    }
}
