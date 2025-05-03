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


class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageList: MutableList<ImageItem>
    private val selectedDressTypes = mutableListOf<String>()
    private var lastLoadTime = 0L
    private val allDressTypes = arrayOf(
        "Tshirts",
        "Casual_Shirts",
        "Formal_Shirts",
        "Jeans",
        "Track_Pants",
        "Shorts",
        "Trousers",
        "Pants"
    )

    private val lastVisibleDocuments = mutableMapOf<String, DocumentSnapshot?>()
    private var isLoading = false
    private val pageSize = 2  // number of images to load at a time
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

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

    private fun setupUserDropdown() {
        // Get the spinner reference from the layout
        val userDropdown: Spinner = findViewById(R.id.userDropdown)

        // Check if spinner is null for debugging (optional)
        if (userDropdown == null) {
            Log.e("MainActivity", "Spinner not found in layout")
        } else {
            Log.d("MainActivity", "Spinner found: $userDropdown")
        }

        // Create a list of user roles and corresponding icons
        val userRoles = listOf("User1", "User2", "User3")
        val userIcons = listOf(
            R.drawable.dummy_person_icon1,  // Icon for User1
            R.drawable.dummy_person_icon2,  // Icon for User2
            R.drawable.dummy_person_icon1   // Icon for User3
        )

        // Create the adapter for the spinner
        val userAdapter = UserRoleAdapter(this, userRoles, userIcons)

        // Attach the spinner reference to the adapter before setting it as the adapter
        userAdapter.attachSpinner(userDropdown)  // Attach spinner reference first

        // Set the adapter to the spinner
        userDropdown.adapter = userAdapter

        // Set listener for the settings icon click
        userAdapter.setSettingsIconClickListener { position ->
            // Perform action when settings icon is clicked
            // Get the selected username
            val selectedUser = userRoles[position]


            // Open settings activity
            val intent = Intent(this@MainActivity, RoleSettingActivity::class.java)
            // Put the selected username as an extra
            intent.putExtra("selected_user", selectedUser)
            startActivity(intent)

            // Close the spinner dropdown by triggering a click action
            userDropdown.post {
                userDropdown.performClick()  // This should trigger the spinner to close
            }
        }

        var isFirstSelection = true

        // Set the item selected listener for the spinner
        userDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }
                val selectedRole = userRoles[position]
                Toast.makeText(this@MainActivity, "Selected: $selectedRole", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
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
        lastVisibleDocuments.clear()  // Reset for fresh fetch
        val typesToFetch =
            if (selectedDressTypes.isEmpty()) allDressTypes.toList() else selectedDressTypes
        var collectionsFetched = 0

        for (type in typesToFetch) {
            val collectionName = type.lowercase()
            db.collection(collectionName).limit(pageSize.toLong()).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        collectionsFetched++
                        if (collectionsFetched == typesToFetch.size) {
                            setupAdapter()
                            swipeRefreshLayout.isRefreshing = false  // <-- Stop loader here
                        }
                        return@addOnSuccessListener
                    }

                    for (document in documents) {
                        ///////////////////////////////////////////////////////////////////////////////////////////////
                        val imageUrls = document.get("image_urls") as? List<*>
                        val imageUrl = imageUrls?.getOrNull(0) as? String

                        if (imageUrl != null) {
                            imageList.add(ImageItem(imageUrl, collectionName, document.id))
                        }


                    }

                    lastVisibleDocuments[collectionName] = documents.documents.last()

                    collectionsFetched++
                    if (collectionsFetched == typesToFetch.size) {
                        setupAdapter()
                        swipeRefreshLayout.isRefreshing = false  // <-- Stop loader here
                    }
                }.addOnFailureListener { exception ->
                    Log.e(
                        "FetchImages",
                        "Error fetching from $collectionName: ${exception.message}",
                        exception
                    )
                    collectionsFetched++
                    if (collectionsFetched == typesToFetch.size) {
                        setupAdapter()
                        swipeRefreshLayout.isRefreshing = false  // <-- Stop loader here
                    }
                }
        }
    }




    private fun setupFilter() {

        val moodOptions = arrayOf("Casual", "Formal", "Party", "Workout")
        val selectedMoods = mutableListOf<String>()

        findViewById<TextView>(R.id.dressMoodDropdown).setOnClickListener {
            // Reflect current selectedMoods into checkedItems before opening dialog
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
                }
                .show()
        }



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
        val mediumImageAdapter = MediumImageAdapter(this, imageList)
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
            val lastVisible = lastVisibleDocuments[collectionName]

            if (lastVisible == null) {
                collectionsFetched++
                if (collectionsFetched == typesToFetch.size) {
                    isLoading = false
                }
                continue
            }

            db.collection(collectionName).startAfter(lastVisible).limit(pageSize.toLong()).get()
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
                                newItems.add(ImageItem(imageUrl, collectionName, document.id))
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


}
