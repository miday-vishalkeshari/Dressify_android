package com.example.dressify.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.dressify.R

class IconGridAdapter(
    private val context: Context,
    private val icons: List<Int>
) : BaseAdapter() {

    var selectedPosition: Int = 0 // Default first selected

    override fun getCount(): Int = icons.size

    override fun getItem(position: Int): Any = icons[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_user_icon, parent, false)
        val iconImageView = view.findViewById<ImageView>(R.id.iconImageView)
        iconImageView.setImageResource(icons[position])

        val bgDrawable = iconImageView.background as GradientDrawable

        if (position == selectedPosition) {
            bgDrawable.setStroke(6, Color.parseColor("#FF6200EE"))
        } else {
            bgDrawable.setStroke(0, Color.TRANSPARENT)
        }

        return view
    }


    fun updateSelectedPosition(position: Int) {
        selectedPosition = position
        notifyDataSetChanged()
    }
}