package com.example.dressify

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.get
import android.graphics.Matrix


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

            imageView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        val drawable = imageView.drawable ?: return@setOnTouchListener true
                        val bitmap = (drawable as android.graphics.drawable.BitmapDrawable).bitmap

                        val imageMatrix = imageView.imageMatrix
                        val values = FloatArray(9)
                        imageMatrix.getValues(values)

                        val scaleX = values[Matrix.MSCALE_X]
                        val scaleY = values[Matrix.MSCALE_Y]
                        val transX = values[Matrix.MTRANS_X]
                        val transY = values[Matrix.MTRANS_Y]

                        val touchX = (event.x - transX) / scaleX
                        val touchY = (event.y - transY) / scaleY

                        val x = touchX.toInt()
                        val y = touchY.toInt()

                        if (x in 0 until bitmap.width && y in 0 until bitmap.height) {
                            val pixelColor = bitmap.getPixel(x, y)
                            val hexColor = String.format("#%06X", 0xFFFFFF and pixelColor)

                            // Move and show marker
                            touchMarker.translationX = event.x - touchMarker.width / 2
                            touchMarker.translationY = event.y - touchMarker.height / 2
                            touchMarker.visibility = ImageView.VISIBLE

                            // Set color to circular view with border
                            val drawableCircle = GradientDrawable().apply {
                                shape = GradientDrawable.OVAL
                                setColor(pixelColor)
                                setStroke(4, Color.parseColor("#DDDDDD"))
                            }
                            selectedColorView.background = drawableCircle
                        }
                    }
                }
                true
            }

        }
    }
}
