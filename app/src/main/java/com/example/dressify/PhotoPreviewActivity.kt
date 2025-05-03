package com.example.dressify

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.Matrix
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class PhotoPreviewActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var touchMarker: ImageView
    private lateinit var selectedColorView: View
    private lateinit var instructionText: TextView
    private lateinit var skinTypeLabelText: TextView
    private lateinit var confirmButton: Button

    private var selectedSkinType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_preview)

        initializeViews()
        loadImageFromIntent()
        setupImageTouchListener()
        setupConfirmButton()
    }

    private fun initializeViews() {
        imageView = findViewById(R.id.previewImageView)
        touchMarker = findViewById(R.id.touchMarker)
        selectedColorView = findViewById(R.id.selectedColorView)
        instructionText = findViewById(R.id.instructionText)
        skinTypeLabelText = findViewById(R.id.skinTypeLabelText)
        confirmButton = findViewById(R.id.saveColorButton)
    }

    private fun loadImageFromIntent() {
        val byteArray = intent.getByteArrayExtra("capturedImage")
        byteArray?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            imageView.setImageBitmap(bitmap)
            imageView.post { detectColorAtCenter() }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupImageTouchListener() {
        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                handleTouchEvent(event.x, event.y)
            }
            true
        }
    }

    private fun setupConfirmButton() {
        confirmButton.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra("selectedSkinColor", selectedSkinType ?: "Unknown")
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun detectColorAtCenter() {
        val centerX = imageView.width / 2f
        val centerY = imageView.height / 2f
        handleTouchEvent(centerX, centerY)
    }

    private fun handleTouchEvent(x: Float, y: Float) {
        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap ?: return
        val (bmpX, bmpY) = mapToBitmapCoordinates(x, y, bitmap) ?: return

        if (bmpX in 0 until bitmap.width && bmpY in 0 until bitmap.height) {
            val pixelColor = bitmap.getPixel(bmpX, bmpY)
            updateUIWithColor(pixelColor, x, y)
        }
    }

    private fun mapToBitmapCoordinates(x: Float, y: Float, bitmap: Bitmap): Pair<Int, Int>? {
        val values = FloatArray(9)
        imageView.imageMatrix.getValues(values)

        val scaleX = values[Matrix.MSCALE_X]
        val scaleY = values[Matrix.MSCALE_Y]
        val transX = values[Matrix.MTRANS_X]
        val transY = values[Matrix.MTRANS_Y]

        val bmpX = ((x - transX) / scaleX).toInt()
        val bmpY = ((y - transY) / scaleY).toInt()

        return Pair(bmpX, bmpY)
    }

    private fun updateUIWithColor(pixelColor: Int, x: Float, y: Float) {
        moveTouchMarker(x, y)
        updateSelectedColorView(pixelColor)
        detectSkinType(pixelColor)
        confirmButton.visibility = View.VISIBLE
    }

    private fun moveTouchMarker(x: Float, y: Float) {
        touchMarker.translationX = x - touchMarker.width / 2
        touchMarker.translationY = y - touchMarker.height / 2
        touchMarker.visibility = View.VISIBLE
    }

    private fun updateSelectedColorView(pixelColor: Int) {
        val drawableCircle = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(pixelColor)
            setStroke(4, Color.parseColor("#DDDDDD"))
        }
        selectedColorView.background = drawableCircle
    }

    private fun detectSkinType(pixelColor: Int) {
        val brightness = calculateBrightness(pixelColor)
        selectedSkinType = when {
            brightness >= 200 -> "Very Fair"
            brightness in 170.0..199.99 -> "Fair"
            brightness in 140.0..169.99 -> "Medium Fair"
            brightness in 110.0..139.99 -> "Medium"
            brightness in 80.0..109.99 -> "Medium Dark"
            brightness in 50.0..79.99 -> "Dark"
            else -> "Very Dark"
        }
        skinTypeLabelText.text = "Skin type: $selectedSkinType"
    }

    private fun calculateBrightness(color: Int): Double {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return 0.299 * r + 0.587 * g + 0.114 * b
    }
}