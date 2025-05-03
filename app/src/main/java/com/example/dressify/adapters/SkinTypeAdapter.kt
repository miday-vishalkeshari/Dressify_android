package com.example.dressify.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.dressify.R

class SkinTypeAdapter(
    context: Context,
    private val skinTypes: List<String>, // List of skin types
    private val skinTypeIcons: List<Int> // List of drawable icons corresponding to the skin types
) : ArrayAdapter<String>(context, R.layout.spinner_skin_type_item, skinTypes) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the default view and customize it
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_skin_type_item, parent, false)

        val iconImageView = view.findViewById<ImageView>(R.id.skinTypeIcon)
        val textView = view.findViewById<TextView>(R.id.skinTypeText)

        // Set icon and text for the current position
        iconImageView.setImageResource(skinTypeIcons[position])
        textView.text = skinTypes[position]

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get the dropdown view and customize it
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_skin_type_item, parent, false)

        val iconImageView = view.findViewById<ImageView>(R.id.skinTypeIcon)
        val textView = view.findViewById<TextView>(R.id.skinTypeText)

        // Set icon and text for the dropdown view
        iconImageView.setImageResource(skinTypeIcons[position])
        textView.text = skinTypes[position]

        return view
    }
}