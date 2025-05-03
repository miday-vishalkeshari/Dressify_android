package com.example.dressify

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Matrix

class PhotoPreviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var touchMarker: ImageView
    private lateinit var selectedColorView: View
    private lateinit var instructionText: TextView
    private lateinit var skinTypeLabelText: TextView
    private lateinit var confirmButton: Button

    private var selectedSkinType: String? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_preview)

        imageView = findViewById(R.id.previewImageView)
        touchMarker = findViewById(R.id.touchMarker)
        selectedColorView = findViewById(R.id.selectedColorView)
        instructionText = findViewById(R.id.instructionText)
        skinTypeLabelText = findViewById(R.id.skinTypeLabelText)
        confirmButton = findViewById(R.id.saveColorButton)

        val byteArray = intent.getByteArrayExtra("capturedImage")
        if (byteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            imageView.setImageBitmap(bitmap)

            // Once image is loaded and layout is ready
            imageView.post {
                val drawable = imageView.drawable as? BitmapDrawable ?: return@post
                val bmp = drawable.bitmap

                val imageMatrix = imageView.imageMatrix
                val values = FloatArray(9)
                imageMatrix.getValues(values)

                val scaleX = values[Matrix.MSCALE_X]
                val scaleY = values[Matrix.MSCALE_Y]
                val transX = values[Matrix.MTRANS_X]
                val transY = values[Matrix.MTRANS_Y]

                // Get center of imageView
                val centerX = imageView.width / 2f
                val centerY = imageView.height / 2f

                // Map to bitmap coords
                val bmpX = ((centerX - transX) / scaleX).toInt()
                val bmpY = ((centerY - transY) / scaleY).toInt()

                if (bmpX in 0 until bmp.width && bmpY in 0 until bmp.height) {
                    val pixelColor = bmp.getPixel(bmpX, bmpY)

                    // Move and show marker
                    touchMarker.translationX = centerX - touchMarker.width / 2
                    touchMarker.translationY = centerY - touchMarker.height / 2
                    touchMarker.visibility = ImageView.VISIBLE

                    // Set color to circular view with border
                    val drawableCircle = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(pixelColor)
                        setStroke(4, Color.parseColor("#DDDDDD"))
                    }
                    selectedColorView.background = drawableCircle

                    // Detect skin type based on brightness
                    val r = Color.red(pixelColor)
                    val g = Color.green(pixelColor)
                    val b = Color.blue(pixelColor)
                    val brightness = (0.299 * r + 0.587 * g + 0.114 * b)

                    val skinType = when {
                        brightness >= 200 -> "Very Fair"
                        brightness in 170.0..199.99 -> "Fair"
                        brightness in 140.0..169.99 -> "Medium Fair"
                        brightness in 110.0..139.99 -> "Medium"
                        brightness in 80.0..109.99 -> "Medium Dark"
                        brightness in 50.0..79.99 -> "Dark"
                        else -> "Very Dark"
                    }

                    selectedSkinType = skinType
                    skinTypeLabelText.text = "Skin type: $skinType"

                    // Show confirm button
                    confirmButton.visibility = View.VISIBLE
                }
            }

            imageView.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        val drawable = imageView.drawable ?: return@setOnTouchListener true
                        val bitmapDrawable = drawable as BitmapDrawable
                        val bmp = bitmapDrawable.bitmap

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

                        if (x in 0 until bmp.width && y in 0 until bmp.height) {
                            val pixelColor = bmp.getPixel(x, y)

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

                            // Detect skin type based on brightness
                            val r = Color.red(pixelColor)
                            val g = Color.green(pixelColor)
                            val b = Color.blue(pixelColor)
                            val brightness = (0.299 * r + 0.587 * g + 0.114 * b)

                            val skinType = when {
                                brightness >= 200 -> "Very Fair"
                                brightness in 170.0..199.99 -> "Fair"
                                brightness in 140.0..169.99 -> "Medium Fair"
                                brightness in 110.0..139.99 -> "Medium"
                                brightness in 80.0..109.99 -> "Medium Dark"
                                brightness in 50.0..79.99 -> "Dark"
                                else -> "Very Dark"
                            }

                            selectedSkinType = skinType
                            skinTypeLabelText.text = "Skin type: $skinType"
                            confirmButton.visibility = View.VISIBLE
                        }
                    }
                }
                true
            }
        }

        confirmButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("selectedSkinColor", selectedSkinType ?: "Unknown")
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
