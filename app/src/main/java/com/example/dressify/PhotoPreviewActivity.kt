package com.example.dressify

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class PhotoPreviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var touchMarker: ImageView
    private lateinit var selectedColorView: View
    private lateinit var instructionText: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_preview)

        imageView = findViewById(R.id.previewImageView)
        touchMarker = findViewById(R.id.touchMarker)
        selectedColorView = findViewById(R.id.selectedColorView)
        instructionText = findViewById(R.id.instructionText)

        val byteArray = intent.getByteArrayExtra("capturedImage")
        if (byteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            imageView.setImageBitmap(bitmap)

            imageView.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    val imageViewBitmap = (imageView.drawable as android.graphics.drawable.BitmapDrawable).bitmap

                    val scaleX = imageViewBitmap.width.toFloat() / imageView.width
                    val scaleY = imageViewBitmap.height.toFloat() / imageView.height

                    val x = (event.x * scaleX).toInt()
                    val y = (event.y * scaleY).toInt()

                    if (x in 0 until imageViewBitmap.width && y in 0 until imageViewBitmap.height) {
                        val pixelColor = imageViewBitmap.getPixel(x, y)
                        val hexColor = String.format("#%06X", 0xFFFFFF and pixelColor)

                        Toast.makeText(this, "Color: $hexColor", Toast.LENGTH_SHORT).show()

                        // Move and show marker
                        touchMarker.translationX = event.x - touchMarker.width / 2
                        touchMarker.translationY = event.y - touchMarker.height / 2
                        touchMarker.visibility = ImageView.VISIBLE

                        // Update color preview tile
                        selectedColorView.setBackgroundColor(pixelColor)
                    }
                }
                true
            }
        }
    }
}
