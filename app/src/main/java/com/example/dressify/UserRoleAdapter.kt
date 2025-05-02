package com.example.dressify

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class UserRoleAdapter(
    private val context: Context,
    private val roles: List<String>,
    private val iconResIds: List<Int>  // List of icons, one for each user
) : BaseAdapter() {

    private var settingsIconClickListener: ((Int) -> Unit)? = null

    // Set listener for settings icon click
    fun setSettingsIconClickListener(listener: (Int) -> Unit) {
        settingsIconClickListener = listener
    }

    override fun getCount(): Int = roles.size
    override fun getItem(position: Int): Any = roles[position]
    override fun getItemId(position: Int): Long = position.toLong()

    // View for selected item (collapsed state)
    // View for selected item (collapsed state)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        val profileIcon: ImageView = view.findViewById(R.id.spinnerProfileIcon)
        val textView: TextView = view.findViewById(R.id.spinnerText)
        val settingsIcon: ImageView = view.findViewById(R.id.spinnerSettingsIcon)
        val divider: View = view.findViewById(R.id.spinnerDivider)

        // Set the correct icon for the user based on the position
        profileIcon.setImageResource(iconResIds[position])
        textView.text = roles[position]
        settingsIcon.visibility = View.GONE   // Hide settings icon in selected item
        divider.visibility = View.GONE

        // Reduce margins to minimize extra space
        val profileIconLayoutParams = profileIcon.layoutParams as ViewGroup.MarginLayoutParams
        profileIconLayoutParams.marginEnd = 4  // Adjust margin between the icon and text
        profileIcon.layoutParams = profileIconLayoutParams

        // Adjust margin between TextView and profile icon
        val textLayoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
        textLayoutParams.marginStart = 2  // Reduced margin between text and icon to make the space smaller
        textView.layoutParams = textLayoutParams

        return view
    }


    // View for dropdown items (expanded state)
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false)
        val profileIcon: ImageView = view.findViewById(R.id.spinnerProfileIcon)
        val textView: TextView = view.findViewById(R.id.spinnerText)
        val settingsIcon: ImageView = view.findViewById(R.id.spinnerSettingsIcon)
        val divider: View = view.findViewById(R.id.spinnerDivider)



        // Set icons, sizes, text size as before
        profileIcon.setImageResource(iconResIds[position])
        profileIcon.layoutParams.height = 120
        profileIcon.layoutParams.width = 120
        profileIcon.scaleType = ImageView.ScaleType.CENTER_CROP

        textView.text = roles[position]
        textView.textSize = 18f
        textView.setPadding(8, 8, 8, 8)

        settingsIcon.visibility = View.VISIBLE
        divider.visibility = View.VISIBLE

        settingsIcon.layoutParams.height = 120
        settingsIcon.layoutParams.width = 120
        settingsIcon.scaleType = ImageView.ScaleType.CENTER_INSIDE

        settingsIcon.setOnClickListener {
            val originalBackground = view.background
            view.setBackgroundColor(context.getColor(android.R.color.darker_gray))
            view.postDelayed({
                view.background = originalBackground
            }, 200)
            settingsIconClickListener?.invoke(position)
        }

        return view
    }



}
