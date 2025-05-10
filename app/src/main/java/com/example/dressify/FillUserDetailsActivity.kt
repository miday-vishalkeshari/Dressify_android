package com.example.dressify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.dressify.adapters.SkinColourAdapter
import com.example.dressify.adapters.SkinTypeAdapter
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.UUID

class FillUserDetailsActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var editTextAge: EditText
    private lateinit var skinColourSpinner: Spinner
    private lateinit var skinTypeSpinner: Spinner
    private lateinit var editTextHeight: EditText
    private lateinit var saveButton: Button
    private var userdocumentId: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var photoPreviewLauncher: ActivityResultLauncher<Intent>
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var userEmail: String? = null
    private var userName: String? = null

    private val skinColours = listOf(
        "Very Fair", "Fair", "Medium Fair", "Medium", "Medium Dark", "Dark", "Very Dark"
    )
    private val skinColourHexCodes = listOf(
        0xFFFFE0BD.toInt(), 0xFFEECD9C.toInt(), 0xFFCEAA88.toInt(), 0xFFBE8C63.toInt(),
        0xFF8D5524.toInt(), 0xFF7D451A.toInt(), 0xFF5C3317.toInt()
    )
    private val skinTypes = listOf(
        "Petite", "Column Female", "Inverted Triangle Female", "Apple", "Brick",
        "Pear", "Hourglass", "Full Hourglass", "Rectangle", "Square",
        "Inverted Triangle Male", "Triangle", "Column Male", "Trapezium", "Circle", "Oval"
    )
    private val skinTypeIcons = listOf(
        R.drawable.ic_rectangle, R.drawable.ic_column, R.drawable.ic_inverted_triangle, R.drawable.ic_apple, R.drawable.ic_rectangle,
        R.drawable.ic_triangle, R.drawable.ic_hourglass, R.drawable.ic_full_hourglass, R.drawable.ic_rectangle, R.drawable.ic_square,
        R.drawable.ic_inverted_triangle, R.drawable.ic_triangle, R.drawable.ic_column, R.drawable.ic_trapezium, R.drawable.ic_circle, R.drawable.ic_oval
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fill_user_details)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        userEmail = intent.getStringExtra("email") // Fetch email from Intent
        userName = intent.getStringExtra("name")  // Fetch name from Intent

        // Initialize views
        editTextName = findViewById(R.id.editTextName)
        editTextName.setText(userName) // Set the name in the EditText

        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        editTextAge = findViewById(R.id.editTextAge)
        skinColourSpinner = findViewById(R.id.skinColourSpinner)
        skinTypeSpinner = findViewById(R.id.skinTypeSpinner)
        editTextHeight = findViewById(R.id.editTextHeight)
        saveButton = findViewById(R.id.saveButton)



        val scrollView = findViewById<ScrollView>(R.id.scrollView) // Add an ID to your ScrollView in XML

        // Handle keyboard insets
        ViewCompat.setOnApplyWindowInsetsListener(scrollView) { view, insets ->
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemInsets.left,
                systemInsets.top,
                systemInsets.right,
                imeInsets.bottom
            )
            insets
        }

        val editTextFields = listOf(
            findViewById<EditText>(R.id.editTextName),
            findViewById<EditText>(R.id.editTextAge),
            findViewById<EditText>(R.id.editTextHeight)
        )

        // Add focus change listener to each EditText
        editTextFields.forEach { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    scrollView.post {
                        scrollView.smoothScrollTo(0, editText.bottom)
                    }
                }
            }
        }

        setupSkinColourSpinner()
        setupSkinTypeSpinner()
        setupCameraLauncher()
        setupBodyTypeInfoLauncher()

        val nameInputLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.nameInputLayout)
        editTextName = findViewById(R.id.editTextName)

        editTextName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val name = editTextName.text.toString()
                if (name.length > 15) {
                    nameInputLayout.error = "Name must be 15 characters or less"
                } else {
                    nameInputLayout.error = null // Clear error
                }
            }
        }

        val ageInputLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.ageInputLayout)
        editTextAge = findViewById(R.id.editTextAge)

        editTextAge.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val age = editTextAge.text.toString().toIntOrNull()
                if (age == null || age !in 1..100) {
                    ageInputLayout.error = "Age must be between 1 and 100"
                } else {
                    ageInputLayout.error = null // Clear error
                }
            }
        }

        // Initialize views
        val heightInputLayout = findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.heightInputLayout)
        val editTextHeight = findViewById<EditText>(R.id.editTextHeight)

        // Add focus change listener for height validation
        editTextHeight.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val height = editTextHeight.text.toString().toIntOrNull()
                if (height == null || height !in 50..250) {
                    heightInputLayout.error = "Height must be between 50 and 250 cm"
                } else {
                    heightInputLayout.error = null // Clear error
                }
            }
        }

        // Save button click listener
        saveButton.setOnClickListener {
            addNewUserToFirebase()
        }
    }

    private fun addNewUserToFirebase() {
        val name = findViewById<EditText>(R.id.editTextName).text.toString()
        val ageText = findViewById<EditText>(R.id.editTextAge).text.toString()
        val heightText = findViewById<EditText>(R.id.editTextHeight).text.toString()

        // Validate name length
        if (name.length > 15) {
            Toast.makeText(this, "Name must be 15 characters or less", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate age range
        val age = ageText.toIntOrNull()
        if (age == null || age !in 1..100) {
            Toast.makeText(this, "Age must be between 1 and 100", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate height range
        val height = heightText.toIntOrNull()
        if (height == null || height !in 50..250) { // Example range: 50cm to 250cm
            Toast.makeText(this, "Height must be between 50 and 250 cm", Toast.LENGTH_SHORT).show()
            return
        }

        val usersCollection = db.collection("Dressify_users")

        // Generate a unique ID based on the current time
        val uniqueId = System.currentTimeMillis().toString()

        val nameDetails = hashMapOf(
            "emoji" to "0",
            "id" to uniqueId,
            "name" to name,
            "age" to age.toString(),
            "height" to height.toString(),
            "gender" to when (findViewById<RadioGroup>(R.id.genderRadioGroup).checkedRadioButtonId) {
                R.id.radioMale -> "Male"
                R.id.radioFemale -> "Female"
                R.id.radioOther -> "Other"
                else -> null
            },
            "skinColour" to (skinColourSpinner.selectedItem?.toString() ?: ""),
            "skinType" to (skinTypeSpinner.selectedItem?.toString() ?: "")
        )

        val newUser = hashMapOf(
            "email" to userEmail,
            "names" to listOf(nameDetails)
        )

        usersCollection.add(newUser)
            .addOnSuccessListener { documentReference ->
                Log.d("LoginActivity", "New user document created with ID: ${documentReference.id}")

                // Pass the documentId to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("documentId", documentReference.id)
                startActivity(intent)
                finish() // Close the current activity
            }
            .addOnFailureListener { exception ->
                Log.e("LoginActivity", "Error creating user document: ${exception.message}")
            }
    }




    private fun setupSkinColourSpinner() {
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
        skinTypeSpinner.adapter = SkinTypeAdapter(this, skinTypes, skinTypeIcons)
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
            Toast.makeText(this, "Camera permission denied. If you have denied it multiple times, please clear app data to allow access.", Toast.LENGTH_SHORT).show()
        }
    }
}