package com.example.dressify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream


class RoleSettingActivity : AppCompatActivity() {

    private lateinit var iconGridView: GridView
    private var selectedIconResId: Int? = null
    private lateinit var skinColourSpinner: Spinner
    private lateinit var skinTypeSpinner: Spinner
    private lateinit var scrollView: ScrollView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_setting)

        // ScrollView reference
        scrollView = findViewById(R.id.roleSettingScrollView)

        // Icon Grid setup
        iconGridView = findViewById(R.id.iconGridView)
        val iconList = listOf(
            R.drawable.dummy_person_icon1,
            R.drawable.dummy_person_icon2,
            R.drawable.dummy_person_icon3,
            R.drawable.dummy_person_icon4,
            R.drawable.dummy_person_icon5,
            R.drawable.dummy_person_icon6
        )
        val adapter = IconGridAdapter(this, iconList)
        iconGridView.adapter = adapter

        // By default select first icon
        selectedIconResId = iconList[0]

        iconGridView.setOnItemClickListener { _, _, position, _ ->
            selectedIconResId = iconList[position]
            adapter.updateSelectedPosition(position)
            //Toast.makeText(this, "Selected icon: $selectedIconResId", Toast.LENGTH_SHORT).show()
        }



        skinColourSpinner = findViewById(R.id.skinColourSpinner)
        // Skin colours and their corresponding hex values
        val skinColours = listOf("Fair", "Wheatish", "Dusky", "Dark")
        val skinColourHexCodes = listOf(
            0xFFFFE0BD.toInt(), // Fair
            0xFFEECD9C.toInt(), // Wheatish
            0xFFBE8C63.toInt(), // Dusky
            0xFF8D5524.toInt()  // Dark
        )

        // Initialize skin type spinner
        skinTypeSpinner = findViewById(R.id.skinTypeSpinner)
        // Skin types list
        val skinTypes = listOf("Pearl", "Apple", "Rectangle")

        // Corresponding icons (drawable resource IDs)
        val skinTypeIcons = listOf(
            R.drawable.ic_pearl, // Replace with actual drawable resource IDs
            R.drawable.ic_apple,
            R.drawable.ic_rectangle
        )

        // Use the custom adapter for the spinner
        val skinTypeSpinnerAdapter = SkinTypeAdapter(this, skinTypes,skinTypeIcons)
        skinTypeSpinner.adapter = skinTypeSpinnerAdapter



        val spinnerAdapter = SkinColourAdapter(this, skinColours, skinColourHexCodes)
        skinColourSpinner.adapter = spinnerAdapter


        // Focus listeners on EditTexts
        val nameEditText = findViewById<EditText>(R.id.editTextName)
        nameEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollToView(v)
            }
        }

        val ageEditText = findViewById<EditText>(R.id.editTextAge)
        ageEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollToView(v)
            }
        }

        val heightEditText = findViewById<EditText>(R.id.editTextHeight)
        heightEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollToView(v)
            }
        }

        // Initialize the camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    // Pass image to new activity
                    val intent = Intent(this, PhotoPreviewActivity::class.java)
                    val stream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val byteArray = stream.toByteArray()
                    intent.putExtra("capturedImage", byteArray)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }


        // Camera icon click listener
        val cameraIconSkinColour = findViewById<ImageView>(R.id.cameraIconSkinColour)
        cameraIconSkinColour.setOnClickListener {
            // Check runtime camera permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            } else {
                openCamera()
            }
        }

        val bodyTypeInfoIcon = findViewById<ImageView>(R.id.bodyTypeInfoIcon)
        bodyTypeInfoIcon.setOnClickListener {
            val intent = Intent(this, BodyTypeGalleryActivity::class.java)
            startActivity(intent)
        }

    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            cameraLauncher.launch(cameraIntent)
        } else {
            Toast.makeText(this, "No camera app found!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scrollToView(view: View) {
        scrollView.post {
            scrollView.smoothScrollTo(0, view.top)
        }
    }
}
