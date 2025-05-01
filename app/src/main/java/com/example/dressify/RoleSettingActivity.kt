package com.example.dressify

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.GridView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ScrollView

class RoleSettingActivity : AppCompatActivity() {

    private lateinit var iconGridView: GridView
    private var selectedIconResId: Int? = null
    private lateinit var skinColourSpinner: Spinner
    private lateinit var scrollView: ScrollView

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

        iconGridView.setOnItemClickListener { _, _, position, _ ->
            selectedIconResId = iconList[position]
            Toast.makeText(this, "Selected icon: $selectedIconResId", Toast.LENGTH_SHORT).show()
        }

        // Spinner setup
        skinColourSpinner = findViewById(R.id.skinColourSpinner)
        val skinColours = listOf("Fair", "Wheatish", "Dusky", "Dark")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, skinColours)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
    }

    private fun scrollToView(view: View) {
        scrollView.post {
            scrollView.smoothScrollTo(0, view.top)
        }
    }
}
