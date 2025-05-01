package com.example.dressify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
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

class MainActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageList: MutableList<ImageItem>
    private val selectedDressTypes = mutableListOf<String>()
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize RecyclerView and the list
        recyclerView = findViewById(R.id.imageRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        imageList = mutableListOf()

        // Initialize your spinners, buttons etc. here
        setupFilter()

        // Fetch image URLs from Firestore
        fetchImageUrlsFromFirestore()

        // Initialize users spinners
        setupUserDropdown()

    }

    private fun fetchImageUrlsFromFirestore() {
        imageList.clear()
        lastVisibleDocuments.clear()  // Reset for fresh fetch
        val typesToFetch = if (selectedDressTypes.isEmpty()) allDressTypes.toList() else selectedDressTypes
        var collectionsFetched = 0

        for (type in typesToFetch) {
            val collectionName = type.lowercase()
            db.collection(collectionName)
                .limit(pageSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        collectionsFetched++
                        if (collectionsFetched == typesToFetch.size) {
                            setupAdapter()
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
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("FetchImages", "Error fetching from $collectionName: ${exception.message}", exception)
                    collectionsFetched++
                    if (collectionsFetched == typesToFetch.size) {
                        setupAdapter()
                    }
                }
        }
    }


    private fun setupUserDropdown() {
        val userDropdown: Spinner = findViewById(R.id.userDropdown)

        val userRoles = listOf("User1", "User2", "User3")
        // Create a list of icons corresponding to each user
        val userIcons = listOf(
            R.drawable.dummy_person_icon1,  // Icon for User1
            R.drawable.dummy_person_icon2,  // Icon for User2
            R.drawable.dummy_person_icon1   // Icon for User3
        )

        val userAdapter = UserRoleAdapter(this, userRoles, userIcons)
        userDropdown.adapter = userAdapter

        // Set listener for the settings icon click
        userAdapter.setSettingsIconClickListener { position ->
            // Perform action when settings icon is clicked
            Toast.makeText(this@MainActivity, "Settings icon clicked for: ${userRoles[position]}", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, RoleSettingActivity::class.java)
            startActivity(intent)
        }

        var isFirstSelection = true

        userDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isFirstSelection) {
                    isFirstSelection = false
                    return
                }
                val selectedRole = userRoles[position]
                Toast.makeText(this@MainActivity, "Selected: $selectedRole", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }




    private fun setupFilter() {



        // Dress Mood Multi-Choice Dropdown
        val moodOptions = arrayOf("Casual", "Formal", "Party", "Workout")
        val selectedMoods = mutableListOf<String>()

        findViewById<TextView>(R.id.dressMoodDropdown).setOnClickListener {
            val checkedItems = BooleanArray(moodOptions.size)
            AlertDialog.Builder(this)
                .setTitle("Select Dress Mood")
                .setMultiChoiceItems(moodOptions, checkedItems) { _, which, isChecked ->
                    if (isChecked) selectedMoods.add(moodOptions[which])
                    else selectedMoods.remove(moodOptions[which])
                }
                .setPositiveButton("OK") { _, _ ->
                    findViewById<TextView>(R.id.dressMoodDropdown).text = selectedMoods.joinToString(", ")
                }
                .show()
        }


        findViewById<TextView>(R.id.dressTypeDropdown).setOnClickListener {
            val checkedItems = BooleanArray(allDressTypes.size) { index ->
                selectedDressTypes.contains(allDressTypes[index])
            }

            AlertDialog.Builder(this)
                .setTitle("Select Dress Type")
                .setMultiChoiceItems(allDressTypes, checkedItems) { _, which, isChecked ->
                    val item = allDressTypes[which]
                    if (isChecked) {
                        if (!selectedDressTypes.contains(item)) {
                            selectedDressTypes.add(item)
                        }
                    } else {
                        selectedDressTypes.remove(item)
                    }
                }
                .setPositiveButton("OK") { _, _ ->
                    findViewById<TextView>(R.id.dressTypeDropdown).text =
                        if (selectedDressTypes.isEmpty()) "All" else selectedDressTypes.joinToString(", ")
                    fetchImageUrlsFromFirestore()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


        // Price Range Single-Choice Dropdown
        val priceOptions = arrayOf("Under ₹500", "₹500 - ₹1000", "₹1000 - ₹2000", "Above ₹2000")
        var selectedPrice = ""

        findViewById<TextView>(R.id.priceRangeDropdown).setOnClickListener {
            var selectedIndex = priceOptions.indexOf(selectedPrice)
            AlertDialog.Builder(this)
                .setTitle("Select Price Range")
                .setSingleChoiceItems(priceOptions, selectedIndex) { _, which ->
                    selectedPrice = priceOptions[which]
                }
                .setPositiveButton("OK") { _, _ ->
                    findViewById<TextView>(R.id.priceRangeDropdown).text = selectedPrice
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }



    private fun setupAdapter() {
        val mediumImageAdapter = MediumImageAdapter(this, imageList)
        recyclerView.adapter = mediumImageAdapter

        recyclerView.clearOnScrollListeners()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {  // Scrolling down
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && (visibleItemCount + pastVisibleItems) >= totalItemCount - 2) {
                        loadMoreImages()
                    }
                }
            }
        })
    }


    private fun loadMoreImages() {
        if (isLoading) return  // Prevent multiple triggers
        isLoading = true

        val typesToFetch = if (selectedDressTypes.isEmpty()) allDressTypes.toList() else selectedDressTypes
        var collectionsFetched = 0

        for (type in typesToFetch) {
            val collectionName = type.lowercase()
            val lastVisible = lastVisibleDocuments[collectionName]

            if (lastVisible == null) {
                collectionsFetched++
                if (collectionsFetched == typesToFetch.size) isLoading = false
                continue
            }

            db.collection(collectionName)
                .startAfter(lastVisible)
                .limit(pageSize.toLong())
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        lastVisibleDocuments[collectionName] = null  // No more data
                    } else {
                        for (document in documents) {
                            val imageUrl = document.getString("image_url")
                            if (imageUrl != null) {
                                imageList.add(ImageItem(imageUrl, collectionName, document.id))
                            }

                        }
                        lastVisibleDocuments[collectionName] = documents.documents.last()
                    }

                    collectionsFetched++
                    if (collectionsFetched == typesToFetch.size) {
                        recyclerView.adapter?.notifyDataSetChanged()
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    collectionsFetched++
                    if (collectionsFetched == typesToFetch.size) isLoading = false
                }
        }
    }


}
