package com.example.dressify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.os.Handler
import android.os.Looper
import com.example.dressify.adapters.MediumImageAdapter
import com.example.dressify.models.ImageItem
import com.example.dressify.models.UserRole


class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageList: MutableList<ImageItem>
    private val selectedDressTypes = mutableListOf<String>()
    private var lastLoadTime = 0L
    private val allDressTypes = arrayOf(
        "tshirts",
        "jeans"
    )
    private var isBackPressedOnce = false
    private val lastVisibleDocuments = mutableMapOf<String, DocumentSnapshot?>()
    private var isLoading = false
    private val pageSize = 2  // number of images to load at a time
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var userdocumentId: String? = null
    private var globalUsersCount = 0

    private var gender: String? = null
    private var age: Int? = null
    private var skinColour: String? = null
    private var skinType: String? = null
    private var height: Int? = null


    private var skinColourRecommendations: List<String> = emptyList()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        userdocumentId = intent.getStringExtra("documentId")
        Log.d("MainActivity", "Received documentId: $userdocumentId")

        // Initialize RecyclerView and the image list
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        imageList = mutableListOf()  // <-- This should be MutableList<ImageItem>

        // Initialize your spinners, buttons etc.
        setupFilter()

        // Initialize users spinners
        setupUserDropdown()

        // Fetch image URLs from Firestore
        fetchImageUrlsFromFirestore()

        monitorUsersCount()

        // Initialize dropdown row view
        val dropdownRow = findViewById<View>(R.id.dropdownRow)

        // Add scroll listener to hide/show dropdowns on scroll
        // Declare this above/on class level (or before adding listener)
        var isDropdownVisible = true  // keep this outside

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as GridLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                // Handle dropdown visibility with animation
                if (dy > 50 && isDropdownVisible) {
                    dropdownRow.animate().alpha(0f).translationY(-dropdownRow.height.toFloat())
                        .setDuration(150)  // 300ms for smooth fade-slide up
                        .withEndAction {
                            dropdownRow.visibility = View.GONE
                        }.start()
                    isDropdownVisible = false

                } else if (firstVisibleItemPosition == 0 && !isDropdownVisible) {
                    dropdownRow.visibility = View.VISIBLE
                    dropdownRow.animate().alpha(1f).translationY(0f)
                        .setDuration(150)  // 300ms for smooth fade-slide down
                        .start()
                    isDropdownVisible = true
                }

                // Load more images if needed
                if (dy > 20) { // Scrolling down
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && (visibleItemCount + pastVisibleItems) >= totalItemCount - 2) {
                        loadMoreImages()
                    }
                }
            }
        })


        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            refreshContent()
        }

        val appSection = findViewById<LinearLayout>(R.id.appSection)
        appSection.setOnClickListener {
            refreshContent()
        }

    }

    private fun monitorUsersCount() {
        val userRef = FirebaseFirestore.getInstance()
            .collection("Dressify_users")
            .document(userdocumentId!!)

        userRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreListener", "Error listening to names array: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val namesList = snapshot.get("names") as? List<*>
                val usersCount = namesList?.size ?: 0
                if (usersCount != globalUsersCount) {
                    globalUsersCount = usersCount
                    setupUserDropdown()
                }
            }
        }
    }

    private fun setupUserDropdown() {
        val userDropdown: Spinner = findViewById(R.id.userDropdown)

        if (userDropdown == null) {
            Log.e("MainActivity", "Spinner not found in layout")
            return
        }

        fetchUserRolesFromFirestore { userRoles ->
            if (userRoles.isEmpty()) {
                Log.w("MainActivity", "No user roles fetched.")
                return@fetchUserRolesFromFirestore
            }

            val emojiToIconMap = mapOf(
                "1" to R.drawable.dummy_person_icon1,
                "2" to R.drawable.dummy_person_icon2,
                "3" to R.drawable.dummy_person_icon3,
                "4" to R.drawable.dummy_person_icon4,
                "5" to R.drawable.dummy_person_icon5,
                "6" to R.drawable.dummy_person_icon6,
                "7" to R.drawable.dummy_person_icon7,
                "8" to R.drawable.dummy_person_icon8,
                "9" to R.drawable.dummy_person_icon9,
                "10" to R.drawable.dummy_person_icon10,
                "11" to R.drawable.ic_add,
            )

            // Add "Add Role" item at the end
            val addRoleItem = UserRole("Add User", "", "11")
            val updatedUserRoles = userRoles.toMutableList()
            updatedUserRoles.add(addRoleItem)

            // Now create a list of icons for updatedUserRoles
            val userIcons = updatedUserRoles.map { userRole ->
                emojiToIconMap[userRole.emoji] ?: R.drawable.dummy_person_icon1
            }

            val userAdapter = UserRoleAdapter(this, updatedUserRoles, userIcons)
            userAdapter.attachSpinner(userDropdown)
            userDropdown.adapter = userAdapter

            userAdapter.setSettingsIconClickListener { position ->
                val selectedUser = updatedUserRoles[position]
                if (selectedUser.name == "Add User") {
                    // Skip handling "Add User" settings
                    return@setSettingsIconClickListener
                }

                val intent = Intent(this@MainActivity, RoleSettingActivity::class.java)
                intent.putExtra("selected_user", selectedUser)
                intent.putExtra("userdocumentId", userdocumentId)
                Log.d("MainActivity", "Passing documentId: $userdocumentId")
                startActivity(intent)

                userDropdown.post { userDropdown.performClick() }
            }

            var isFirstSelection = true
            userDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (isFirstSelection) {
                        isFirstSelection = false
                        return
                    }

                    val selectedRole = updatedUserRoles[position]

                    if (selectedRole.name == "Add User") {
                        userDropdown.setSelection(0)
                        val intent = Intent(this@MainActivity, RoleSettingActivity::class.java)
                        intent.putExtra("selected_user", selectedRole)
                        intent.putExtra("userdocumentId", userdocumentId)
                        Log.d("MainActivity", "Passing documentId: $userdocumentId")
                        startActivity(intent)

                        monitorUsersCount()
                    }
                    else{
                        userDropdown.setSelection(position)
                        fetchUserDetails(selectedRole.id)
                        Toast.makeText(this@MainActivity, "Selected: ${selectedRole.name}", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

    }



    private fun getRecommendationsForSkinColour(skinColour: String?) {
        skinColourRecommendations = emptyList() // Clear the list
        skinColourRecommendations = when (skinColour?.lowercase()) {
            "fair" -> listOf("blue")
            "medium" -> listOf("black","blue")
            "dark" -> listOf("red")
            else -> listOf("blue")
        }
        fetchImageUrlsFromFirestore()
    }

    private fun fetchUserDetails(userId: String) {
        db.collection("Dressify_users")
            .document(userdocumentId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val namesList = document.get("names") as? List<Map<String, Any>>
                    val userDetails = namesList?.find { it["id"] == userId }

                    if (userDetails != null) {
                        gender = userDetails["gender"] as? String
                        age = userDetails["age"] as? Int
                        skinColour = userDetails["skinColour"] as? String
                        skinType = userDetails["skinType"] as? String
                        height = userDetails["height"] as? Int

                        Log.d("FilterColors", "skin color: $skinColour")

                        // Call getRecommendationsForSkinColour with the fetched skinColour
                        getRecommendationsForSkinColour(skinColour)


                        // Display or use the fetched details
                        Log.d("MainActivity", "Gender: $gender, Age: $age, Skin Colour: $skinColour, Skin Type: $skinType, Height: $height")
                        Toast.makeText(this, "Details fetched for ${userDetails["name"]}", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.w("MainActivity", "User details not found for ID: $userId")
                    }
                } else {
                    Log.w("MainActivity", "Document not found for ID: $userdocumentId")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error fetching user details: ${exception.message}")
            }
    }


    private fun fetchUserRolesFromFirestore(onComplete: (List<UserRole>) -> Unit) {
        // Check if documentId is available
        val docId = userdocumentId
        if (docId.isNullOrEmpty()) {
            Log.e("MainActivity", "Document ID is null or empty.")
            onComplete(emptyList())
            return
        }

        // Fetch only this document
        db.collection("Dressify_users")
            .document(docId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val roles = mutableListOf<UserRole>()
                    val namesList = document.get("names") as? List<Map<String, Any>>
                    globalUsersCount = namesList?.size ?: 0


                    namesList?.forEach { nameDetails ->
                        var name = nameDetails["name"] as? String
                        val id = nameDetails["id"] as? String
                        val emoji = nameDetails["emoji"] as? String


                        if (name.isNullOrEmpty()) {
                            name ="User"
                        }

                        if (!name.isNullOrEmpty() && !id.isNullOrEmpty()) {
                            roles.add(UserRole(name, id, emoji.toString()))
                        }
                    }
                    onComplete(roles)
                } else {
                    Log.w("MainActivity", "Document not found for ID: $docId")
                    onComplete(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainActivity", "Error fetching user roles: ", exception)
                onComplete(emptyList())
            }
    }




    private fun refreshContent() {
        swipeRefreshLayout.isRefreshing = true

        // Clear existing data
        imageList.clear()
        recyclerView.adapter?.notifyDataSetChanged()

        // Reset pagination states if needed
        lastVisibleDocuments.clear()

        // Re-fetch images
        fetchImageUrlsFromFirestore()
    }

    private fun fetchImageUrlsFromFirestore() {
        imageList.clear()
        recyclerView.adapter?.notifyDataSetChanged()
        lastVisibleDocuments.clear() // Reset for fresh fetch
        val typesToFetch = if (selectedDressTypes.isEmpty()) allDressTypes.toList() else selectedDressTypes
        var collectionsFetched = 0

        for (type in typesToFetch) {
            val documentPath = "Dressify_styles/$type"
            db.document(documentPath).get()
                .addOnSuccessListener { document ->
                    if (!document.exists()) {
                        collectionsFetched++
                        if (collectionsFetched == typesToFetch.size) {
                            setupAdapter()
                            swipeRefreshLayout.isRefreshing = false // Stop loader here
                        }
                        return@addOnSuccessListener
                    }

                    val subcollectionNames = document.get("collections_list") as? List<String> ?: emptyList()
                    for (color in subcollectionNames) {
                        if (skinColourRecommendations.isNotEmpty() && color !in skinColourRecommendations) {
                            Log.d("FilterColors", "Skipping color: $color")
                            continue // Skip colors not in recommendations
                        }
                        Log.d("FilterColors", "Processing color: $color")

                        db.collection("$documentPath/$color").get()
                            .addOnSuccessListener { documents ->
                                if (documents.isEmpty) {
                                    collectionsFetched++
                                    if (collectionsFetched == typesToFetch.size) {
                                        setupAdapter()
                                        swipeRefreshLayout.isRefreshing = false // Stop loader here
                                    }
                                    return@addOnSuccessListener
                                }

                                for (document in documents) {
                                    val imageUrls = document.get("image_urls") as? List<*>
                                    val imageUrl = imageUrls?.getOrNull(0) as? String

                                    if (imageUrl != null) {
                                        imageList.add(ImageItem(imageUrl, type, color, document.id))
                                    }
                                }

                                recyclerView.adapter?.notifyDataSetChanged()
                                lastVisibleDocuments["$type/$color"] = documents.documents.last()

                                collectionsFetched++
                                if (collectionsFetched == typesToFetch.size) {
                                    setupAdapter()
                                    swipeRefreshLayout.isRefreshing = false // Stop loader here
                                }
                            }.addOnFailureListener { exception ->
                                Log.e(
                                    "FetchImages",
                                    "Error fetching from $type/$color: ${exception.message}",
                                    exception
                                )
                                collectionsFetched++
                                if (collectionsFetched == typesToFetch.size) {
                                    setupAdapter()
                                    swipeRefreshLayout.isRefreshing = false // Stop loader here
                                }
                            }
                    }
                }.addOnFailureListener { exception ->
                    Log.e(
                        "FetchImages",
                        "Error fetching document $documentPath: ${exception.message}",
                        exception
                    )
                    collectionsFetched++
                    if (collectionsFetched == typesToFetch.size) {
                        setupAdapter()
                        swipeRefreshLayout.isRefreshing = false // Stop loader here
                    }
                }
        }
    }

    private val dressTypeMapping = mapOf(
        "Casual" to listOf(
            "tshirts",
            "poloShirts",
            "casualShirts",
            "henleys",
            "hoodies",
            "casualJackets",
            "jeans",
            "chinos",
            "joggers",
            "cargoPants",
            "shorts"
        ),
        "Formal" to listOf(
            "formalShirts",
            "blazers",
            "suits",
            "waistcoats",
            "turtlenecks",
            "formalTrousers",
            "suitPants"
        ),
        "Party" to listOf(
            "partyShirts",
            "stylishTshirts",
            "casualBlazers",
            "leatherJackets",
            "statementSweaters",
            "slimFitJeans",
            "chinos",
            "leatherPants"
        ),
        "Workout" to listOf(
            "athleticTshirts",
            "tankTops",
            "compressionShirts",
            "sweatshirts",
            "trackPants",
            "joggers",
            "runningShorts",
            "compressionTights"
        )
    )



    private fun updateSelectedDressTypes(category: String) {
        selectedDressTypes.clear()
        dressTypeMapping[category]?.let { selectedDressTypes.addAll(it) }
    }

    private fun setupFilter() {

        val moodOptions = arrayOf("Casual", "Formal", "Party", "Workout")
        val selectedMoods = mutableListOf<String>()

        //for style mood
        // for style mood
        findViewById<TextView>(R.id.dressMoodDropdown).setOnClickListener {
            val checkedItems = BooleanArray(moodOptions.size) { index ->
                selectedMoods.contains(moodOptions[index])
            }

            AlertDialog.Builder(this)
                .setTitle("Select Dress Mood")
                .setMultiChoiceItems(moodOptions, checkedItems) { _, which, isChecked ->
                    if (isChecked) {
                        if (!selectedMoods.contains(moodOptions[which])) {
                            selectedMoods.add(moodOptions[which])
                        }
                    } else {
                        selectedMoods.remove(moodOptions[which])
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    findViewById<TextView>(R.id.dressMoodDropdown).text =
                        selectedMoods.joinToString(", ")

                    // Update selectedDressTypes based on the first selected mood
                    if (selectedMoods.isNotEmpty()) {
                        updateSelectedDressTypes(selectedMoods[0]) // Example: Use the first selected mood
                        println(selectedDressTypes) // Prints the updated list
                        fetchImageUrlsFromFirestore() // Fetch images based on the updated dress types
                    }
                }
                .show()
        }


    //for style type
        findViewById<TextView>(R.id.dressTypeDropdown).setOnClickListener {
            val checkedItems = BooleanArray(allDressTypes.size) { index ->
                selectedDressTypes.contains(allDressTypes[index])
            }

            AlertDialog.Builder(this).setTitle("Select Dress Type")
                .setMultiChoiceItems(allDressTypes, checkedItems) { _, which, isChecked ->
                    val item = allDressTypes[which]
                    if (isChecked) {
                        if (!selectedDressTypes.contains(item)) {
                            selectedDressTypes.add(item)
                        }
                    } else {
                        selectedDressTypes.remove(item)
                    }
                }.setPositiveButton("OK") { _, _ ->
                    findViewById<TextView>(R.id.dressTypeDropdown).text =
                        if (selectedDressTypes.isEmpty()) "All" else selectedDressTypes.joinToString(
                            ", "
                        )
                    fetchImageUrlsFromFirestore()
                }.setNegativeButton("Cancel", null).show()
        }


        val priceOptions = arrayOf("Under ₹500", "₹500 - ₹1000", "₹1000 - ₹2000", "Above ₹2000")
        val selectedPrices = mutableListOf<String>()

        findViewById<TextView>(R.id.priceRangeDropdown).setOnClickListener {
            // Reflect current selections into checkedItems before opening dialog
            val checkedItems = BooleanArray(priceOptions.size) { index ->
                selectedPrices.contains(priceOptions[index])
            }

            AlertDialog.Builder(this)
                .setTitle("Select Price Range")
                .setMultiChoiceItems(priceOptions, checkedItems) { _, which, isChecked ->
                    if (isChecked) {
                        if (!selectedPrices.contains(priceOptions[which])) {
                            selectedPrices.add(priceOptions[which])
                        }
                    } else {
                        selectedPrices.remove(priceOptions[which])
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    findViewById<TextView>(R.id.priceRangeDropdown).text =
                        selectedPrices.joinToString(", ")
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    }


    private fun setupAdapter() {
        val mediumImageAdapter = MediumImageAdapter(
            this,
            imageList,
            "MainActivity",
            userdocumentId.toString()
        ) { itemToDelete ->
            // Handle delete action
//            db.collection(itemToDelete.collectionName)
//                .document(itemToDelete.documentId)
//                .delete()
//                .addOnSuccessListener {
//                    // Remove the item from the list and notify the adapter
//                    imageList.remove(itemToDelete)
//                    recyclerView.adapter?.notifyDataSetChanged()
//                    Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { exception ->
//                    Toast.makeText(this, "Error deleting item: ${exception.message}", Toast.LENGTH_SHORT).show()
//                }
        }
        recyclerView.adapter = mediumImageAdapter

        // Add scroll listener to manage load more trigger
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val currentTime = System.currentTimeMillis()
                // Only allow trigger after 300ms
                if (currentTime - lastLoadTime < 100) {
                    lastLoadTime = currentTime
                    return
                }
                lastLoadTime = currentTime

                if (dy > 0) {  // Scrolling down
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    // Trigger loadMoreImages only when we are near the bottom
                    if (!isLoading && (visibleItemCount + pastVisibleItems) >= totalItemCount - 5) {
                        loadMoreImages()
                    }
                }
            }
        })
    }


    private fun loadMoreImages() {
        if (isLoading) return  // Prevent multiple triggers (flickering issue)

        Log.d("LoadMore", "Loading more images...")

        isLoading = true

        val typesToFetch =
            if (selectedDressTypes.isEmpty()) allDressTypes.toList() else selectedDressTypes
        var collectionsFetched = 0

        for (type in typesToFetch) {
            val collectionName = type.lowercase()
            val styleColour = "blue"
            val lastVisible = lastVisibleDocuments[collectionName]

            if (lastVisible == null) {
                collectionsFetched++
                if (collectionsFetched == typesToFetch.size) {
                    isLoading = false
                }
                continue
            }

            db.collection("Dressify_styles").document(collectionName).collection(styleColour).startAfter(lastVisible).limit(pageSize.toLong()).get()
                .addOnSuccessListener { documents ->
                    Log.d("LoadMore", "Fetched more images from $collectionName")

                    if (documents.isEmpty) {
                        lastVisibleDocuments[collectionName] = null  // No more data
                    } else {
                        val newItems = mutableListOf<ImageItem>()
                        for (document in documents) {
                            val imageUrls = document.get("image_urls") as? List<*>
                            val imageUrl = imageUrls?.getOrNull(0) as? String
                            if (imageUrl != null) {
                                newItems.add(ImageItem(imageUrl, collectionName,styleColour, document.id))
                            }
                        }

                        // Add all new items to the list
                        imageList.addAll(newItems)

                        // Update RecyclerView once after all items are added
                        recyclerView.adapter?.notifyItemRangeInserted(
                            imageList.size - newItems.size, newItems.size
                        )

                        lastVisibleDocuments[collectionName] = documents.documents.last()
                    }

                    collectionsFetched++
                    if (collectionsFetched == typesToFetch.size) {
                        isLoading = false
                    }
                }.addOnFailureListener {
                    collectionsFetched++
                    if (collectionsFetched == typesToFetch.size) {
                        isLoading = false
                    }
                }
        }
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            if (isBackPressedOnce) {
                // Exit the app on the second back press
                super.onBackPressed()
                return
            }

            // Refresh content on the first back press
            isBackPressedOnce = true
            refreshContent() // Trigger the same action as SwipeRefreshLayout or appSection click
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

            // Reset the flag after 2 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                isBackPressedOnce = false
            }, 2000)
        } else {
            // Normal back behavior if other activities are in the stack
            super.onBackPressed()
        }
    }

}
