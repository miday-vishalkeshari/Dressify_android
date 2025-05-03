package com.example.dressify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dressify.adapters.IconGridAdapter
import com.example.dressify.adapters.SkinColourAdapter
import com.example.dressify.adapters.SkinTypeAdapter
import java.io.ByteArrayOutputStream

class RoleSettingActivity : AppCompatActivity() {

    private lateinit var iconGridView: GridView
    private lateinit var skinColourSpinner: Spinner
    private lateinit var skinTypeSpinner: Spinner
    private lateinit var scrollView: ScrollView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoPreviewLauncher: ActivityResultLauncher<Intent>
    private var selectedIconResId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_setting)

        initializeViews()
        setupIconGrid()
        setupSkinColourSpinner()
        setupSkinTypeSpinner()
        setupFocusAutoScroll()
        setupCameraLauncher()
        setupBodyTypeInfoLauncher()
    }

    private fun initializeViews() {
        scrollView = findViewById(R.id.roleSettingScrollView)
        iconGridView = findViewById(R.id.iconGridView)
        skinColourSpinner = findViewById(R.id.skinColourSpinner)
        skinTypeSpinner = findViewById(R.id.skinTypeSpinner)

        val selectedUser = intent.getStringExtra("selected_user")
        findViewById<EditText>(R.id.editTextName).setText(selectedUser ?: "")
    }

    private fun setupIconGrid() {
        val iconList = listOf(
            R.drawable.dummy_person_icon1, R.drawable.dummy_person_icon2, R.drawable.dummy_person_icon3,
            R.drawable.dummy_person_icon4, R.drawable.dummy_person_icon5, R.drawable.dummy_person_icon6,
            R.drawable.dummy_person_icon7, R.drawable.dummy_person_icon8, R.drawable.dummy_person_icon9,
            R.drawable.dummy_person_icon10
        )
        val adapter = IconGridAdapter(this, iconList)
        iconGridView.adapter = adapter
        selectedIconResId = iconList[0]

        iconGridView.setOnItemClickListener { _, _, position, _ ->
            selectedIconResId = iconList[position]
            adapter.updateSelectedPosition(position)
        }
    }

    private fun setupSkinColourSpinner() {
        val skinColours = listOf(
            "Very Fair", "Fair", "Medium Fair", "Medium", "Medium Dark", "Dark", "Very Dark"
        )
        val skinColourHexCodes = listOf(
            0xFFFFE0BD.toInt(), 0xFFEECD9C.toInt(), 0xFFCEAA88.toInt(), 0xFFBE8C63.toInt(),
            0xFF8D5524.toInt(), 0xFF7D451A.toInt(), 0xFF5C3317.toInt()
        )
        skinColourSpinner.adapter = SkinColourAdapter(this, skinColours, skinColourHexCodes)

        photoPreviewLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedSkinColor = result.data?.getStringExtra("selectedSkinColor")
                selectedSkinColor?.let {
                    val position = skinColours.indexOf(it)
                    if (position != -1) skinColourSpinner.setSelection(position)
                }
            }
        }
    }

    private fun setupSkinTypeSpinner() {
        val skinTypes = listOf(
            "Petite", "Column Female", "Inverted Triangle Female", "Apple", "Brick",
            "Pear", "Hourglass", "Full Hourglass", "Rectangle", "Square",
            "Inverted Triangle Male", "Triangle", "Column Male", "Trapezium", "Circle", "Oval"
        )
        val skinTypeIcons = listOf(
            R.drawable.ic_rectangle, R.drawable.ic_column, R.drawable.ic_inverted_triangle, R.drawable.ic_apple, R.drawable.ic_rectangle,
            R.drawable.ic_triangle, R.drawable.ic_hourglass, R.drawable.ic_full_hourglass, R.drawable.ic_rectangle, R.drawable.ic_square,
            R.drawable.ic_inverted_triangle, R.drawable.ic_triangle, R.drawable.ic_column, R.drawable.ic_trapezium, R.drawable.ic_circle, R.drawable.ic_oval
        )
        skinTypeSpinner.adapter = SkinTypeAdapter(this, skinTypes, skinTypeIcons)
    }

    private fun setupFocusAutoScroll() {
        val editTexts = listOf(
            findViewById<EditText>(R.id.editTextName),
            findViewById<EditText>(R.id.editTextAge),
            findViewById<EditText>(R.id.editTextHeight)
        )
        editTexts.forEach { editText ->
            editText.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) scrollToView(v)
            }
        }
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    val intent = Intent(this, PhotoPreviewActivity::class.java)
                    val stream = ByteArrayOutputStream()
                    it.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    intent.putExtra("capturedImage", stream.toByteArray())
                    photoPreviewLauncher.launch(intent)
                } ?: Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<ImageView>(R.id.cameraIconSkinColour).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
            } else {
                openCamera()
            }
        }
    }

    private fun setupBodyTypeInfoLauncher() {
        val bodyTypeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedBodyTypeIndex = result.data?.getIntExtra("selectedBodyTypeIndex", -1)
                selectedBodyTypeIndex?.takeIf { it in 0 until skinTypeSpinner.adapter.count }?.let {
                    skinTypeSpinner.setSelection(it)
                }
            }
        }

        findViewById<ImageView>(R.id.bodyTypeInfoIcon).setOnClickListener {
            val intent = Intent(this, BodyTypeGalleryActivity::class.java)
            bodyTypeLauncher.launch(intent)
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
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scrollToView(view: View) {
        scrollView.post { scrollView.smoothScrollTo(0, view.top) }
    }
}