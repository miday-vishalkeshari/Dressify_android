package com.example.dressify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dressify.adapters.IconGridAdapter
import com.example.dressify.adapters.SkinColourAdapter
import com.example.dressify.adapters.SkinTypeAdapter
import com.example.dressify.models.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.util.UUID
import com.google.firebase.firestore.FieldValue

class RoleSettingActivity : AppCompatActivity() {

    private lateinit var iconGridView: GridView
    private lateinit var skinColourSpinner: Spinner
    private lateinit var skinTypeSpinner: Spinner
    private lateinit var scrollView: ScrollView
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var photoPreviewLauncher: ActivityResultLauncher<Intent>
    private var selectedIconResId: Int? = null
    private lateinit var logoutButton: Button
    private var userId: String? = null
    private var userGender: String? = null
    private var userAge: String? = null
    private var userEmoji: String? = null
    private var userdocumentId: String? = null
    private var userSkinColour: String? = null
    private var userSkinType: String? = null
    private var userHeight: String? = null
    // Class-level variables to store the lists
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
        setContentView(R.layout.activity_role_setting)

        initializeViews()
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
        logoutButton = findViewById(R.id.logoutButton)

        val selectedUser = intent.getSerializableExtra("selected_user") as? UserRole
        var name = selectedUser?.name
        userId = selectedUser?.id

        userdocumentId = intent.getStringExtra("userdocumentId")
        Log.d("RoleSettingActivity", "Received documentId: $userdocumentId")


        findViewById<Button>(R.id.saveButton).apply {
            text = if (name == "Add User") "Add New User" else "Update User Details"
            setOnClickListener {
                if (name == "Add User") {
                    addNewUserToFirebase()
                } else {
                    updateUserDetailsInArray()
                }
            }
        }
        // Hide the delete button if name is "Add User"
        findViewById<CardView>(R.id.deleteCurrentUserCardview).visibility =
            if (name == "Add User") View.GONE else View.VISIBLE


        if (name == "Add User") {
            findViewById<EditText>(R.id.editTextName).setText("")
            setupIconGrid()
        } else {
            // Fetch user details from Firestore based on userId
            fetchUserDetailsFromFirestore(userId)
            findViewById<EditText>(R.id.editTextName).setText(name ?: "")
        }

        // Set click listener for wishlist icon
        findViewById<ImageView>(R.id.wishlistIcon).setOnClickListener {
            val intent = Intent(this, WishlistActivity::class.java)
            intent.putExtra("userdocumentId", userdocumentId)
            Log.d("RoleSettingActivity", "Sending documentId: $userdocumentId")
            startActivity(intent)
        }


        // Setup the logout button
        logoutButton.setOnClickListener {
            logout()
        }

    }


    private fun addNewUserToFirebase() {
        if (userdocumentId.isNullOrEmpty()) {
            Toast.makeText(this, "Document ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = FirebaseFirestore.getInstance()
            .collection("Dressify_users")
            .document(userdocumentId!!)

        // Generate a random ID
        val newId = UUID.randomUUID().toString()

        // Create a new user map
        val newUserDetails = mapOf(
            "emoji" to userEmoji,
            "id" to newId,
            "name" to findViewById<EditText>(R.id.editTextName).text.toString(),
            "age" to findViewById<EditText>(R.id.editTextAge).text.toString(),
            "height" to findViewById<EditText>(R.id.editTextHeight).text.toString(),
            "gender" to when (findViewById<RadioGroup>(R.id.genderRadioGroup).checkedRadioButtonId) {
                R.id.radioMale -> "Male"
                R.id.radioFemale -> "Female"
                R.id.radioOther -> "Other"
                else -> null
            },
            "skinColour" to (skinColourSpinner.selectedItem?.toString() ?: ""),
            "skinType" to (skinTypeSpinner.selectedItem?.toString() ?: "")
        )

        // Add the new user to the array
        userRef.update("names", FieldValue.arrayUnion(newUserDetails))
            .addOnSuccessListener {
                updateUsersCount()
                Toast.makeText(this, "New user added successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to add new user: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUsersCount() {
        if (userdocumentId.isNullOrEmpty()) {
            Log.e("RoleSettingActivity", "Document ID is null or empty")
            return
        }

        val userRef = FirebaseFirestore.getInstance()
            .collection("Dressify_users")
            .document(userdocumentId!!)

        userRef.update("users_count", FieldValue.increment(1))
            .addOnSuccessListener {
                Log.d("RoleSettingActivity", "users_count incremented successfully")
            }
            .addOnFailureListener { exception ->
                Log.e("RoleSettingActivity", "Failed to increment users_count: ${exception.message}")
            }
    }

    private fun fetchUserDetailsFromFirestore(userId: String?) {
        // Ensure the userId and documentId are valid
        if (userId.isNullOrEmpty()) {
            Log.e("RoleSettingActivity", "User ID is null or empty")
            return
        }

        if (userdocumentId.isNullOrEmpty()) {
            Log.e("RoleSettingActivity", "Document ID is null or empty")
            return
        }

        // Reference to the specific document in Firestore
        val userRef = FirebaseFirestore.getInstance()
            .collection("Dressify_users")
            .document(userdocumentId!!)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val namesList = document.get("names") as? List<Map<String, Any>>
                    namesList?.forEach { nameDetails ->
                        val id = nameDetails["id"] as? String

                        // Only process the user with the matching id
                        if (id == userId) {
                            val name = nameDetails["name"] as? String
                            val gender = nameDetails["gender"] as? String
                            val age = nameDetails["age"] as? String
                            val emoji = nameDetails["emoji"] as? String
                            val skinColour = nameDetails["skinColour"] as? String
                            val skinType = nameDetails["skinType"] as? String
                            val height = nameDetails["height"] as? String

                            Log.d("RoleSettingActivity", "Fetched user details: name=$name, id=$id, gender=$gender, age=$age, emoji=$emoji")

                            // Assign values to global variables
                            userGender = gender
                            userAge = age
                            userEmoji = emoji
                            userSkinColour = skinColour
                            userSkinType = skinType
                            userHeight = height


                            // Update the UI with fetched details
                            updateUIWithUserDetails()

                            return@forEach  // exit loop after finding matching user
                        }
                    }
                } else {
                    Log.e("RoleSettingActivity", "Document not found for ID: $userdocumentId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("RoleSettingActivity", "Error fetching user details: ", exception)
            }
    }



    private fun updateUIWithUserDetails() {
        // Log the values to check if they're being fetched correctly
        setupIconGrid()

        // Set the age in the EditText
        val userAgeToDisplay = userAge ?: ""
        findViewById<EditText>(R.id.editTextAge).setText(userAgeToDisplay)

        // Find RadioGroup and its RadioButtons
        val genderRadioGroup = findViewById<RadioGroup>(R.id.genderRadioGroup)
        val radioMale = findViewById<RadioButton>(R.id.radioMale)
        val radioFemale = findViewById<RadioButton>(R.id.radioFemale)
        val radioOther = findViewById<RadioButton>(R.id.radioOther)

        // Set selected RadioButton based on userGender value
        when (userGender) {
            "Male" -> genderRadioGroup.check(radioMale.id)
            "Female" -> genderRadioGroup.check(radioFemale.id)
            "Other" -> genderRadioGroup.check(radioOther.id)
            else -> genderRadioGroup.clearCheck() // if gender is null/unknown
        }

        // Set the height in the EditText
        findViewById<EditText>(R.id.editTextHeight).setText(userHeight ?: "")

        // Set skin colour in the Spinner using the existing setup method
        // Assuming setupSkinColourSpinner has been called already to initialize the spinner
        userSkinColour?.let {
            val position = skinColours.indexOf(it)
            if (position != -1) {
                skinColourSpinner.setSelection(position)
            }
        }

        // Set skin type in the Spinner using the existing setup method
        // Assuming setupSkinTypeSpinner has been called already to initialize the spinner
        userSkinType?.let {
            val position = skinTypes.indexOf(it)
            if (position != -1) {
                skinTypeSpinner.setSelection(position)
            }
        }
    }

    private fun updateUserDetailsInArray() {
        if (userdocumentId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "Document ID or User ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = FirebaseFirestore.getInstance()
            .collection("Dressify_users")
            .document(userdocumentId!!)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val namesList = document.get("names") as? MutableList<Map<String, Any>>
                    if (namesList != null) {
                        val updatedList = namesList.map { nameDetails ->
                            if (nameDetails["id"] == userId) {
                                nameDetails.toMutableMap().apply {
                                    this["emoji"] = userEmoji as Any
                                    this["name"] = findViewById<EditText>(R.id.editTextName).text.toString()
                                    this["age"] = findViewById<EditText>(R.id.editTextAge).text.toString()
                                    this["height"] = findViewById<EditText>(R.id.editTextHeight).text.toString()
                                    this["gender"] = when (findViewById<RadioGroup>(R.id.genderRadioGroup).checkedRadioButtonId) {
                                        R.id.radioMale -> "Male"
                                        R.id.radioFemale -> "Female"
                                        R.id.radioOther -> "Other"
                                        else -> null
                                    } as Any
                                    this["skinColour"] = skinColourSpinner.selectedItem?.toString() ?: ""
                                    this["skinType"] = skinTypeSpinner.selectedItem?.toString() ?: ""
                                }
                            } else {
                                nameDetails
                            }
                        }

                        userRef.update("names", updatedList)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Details updated successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Failed to update details: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "No names list found in the document", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Document not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching document: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun logout() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut()

        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()  // Close the current activity so the user can't navigate back
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

        val iconIndex = userEmoji?.toIntOrNull() ?: 0
        selectedIconResId = iconList[iconIndex]


        // Update UI to reflect the selected icon
        adapter.updateSelectedPosition(iconIndex)

        iconGridView.setOnItemClickListener { _, _, position, _ ->
            selectedIconResId = iconList[position]
            userEmoji = position.toString()
            adapter.updateSelectedPosition(position)


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