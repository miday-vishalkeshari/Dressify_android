package com.example.dressify

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class IconGridAdapter(
    private val context: Context,
    private val iconList: List<Int>
) : BaseAdapter() {

    override fun getCount(): Int = iconList.size
    override fun getItem(position: Int): Any = iconList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val imageView = ImageView(context)
        imageView.setImageResource(iconList[position])
        imageView.layoutParams = ViewGroup.LayoutParams(150, 150)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setPadding(4, 4, 4, 4)
        return imageView
    }
}
