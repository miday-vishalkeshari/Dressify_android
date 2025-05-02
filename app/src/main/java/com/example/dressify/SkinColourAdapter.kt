package com.example.dressify

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.ImageView
import android.graphics.drawable.GradientDrawable

class SkinColourAdapter(
    context: Context,
    private val colours: List<String>,
    private val colourHexCodes: List<Int>
) : ArrayAdapter<String>(context, 0, colours) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_skin_colour_item, parent, false)

        val swatch = view.findViewById<View>(R.id.colourSwatch)
        val text = view.findViewById<TextView>(R.id.colourName)

        // Set colour
        val bgDrawable = swatch.background as GradientDrawable
        bgDrawable.setColor(colourHexCodes[position])

        // Set text
        text.text = colours[position]

        return view
    }
}
