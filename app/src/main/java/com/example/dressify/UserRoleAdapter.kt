package com.example.dressify

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.PopupWindow
import android.util.Log

class UserRoleAdapter(
    private val context: Context,
    private val roles: List<String>,
    private val iconResIds: List<Int>
) : BaseAdapter() {

    private var settingsIconClickListener: ((Int) -> Unit)? = null
    private var spinner: Spinner? = null  // Spinner reference for closing dropdown

    // Set listener for settings icon click
    fun setSettingsIconClickListener(listener: (Int) -> Unit) {
        settingsIconClickListener = listener
    }

    // Attach spinner instance to adapter
    fun attachSpinner(spinner: Spinner) {
        this.spinner = spinner
    }

    override fun getCount(): Int = roles.size
    override fun getItem(position: Int): Any = roles[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_user_item, parent, false)
        val profileIcon: ImageView = view.findViewById(R.id.spinnerProfileIcon)
        val textView: TextView = view.findViewById(R.id.spinnerText)
        val settingsIcon: ImageView = view.findViewById(R.id.spinnerSettingsIcon)
        val divider: View = view.findViewById(R.id.spinnerDivider)

        profileIcon.setImageResource(iconResIds[position])
        textView.text = roles[position]
        settingsIcon.visibility = View.GONE
        divider.visibility = View.GONE

        val profileIconLayoutParams = profileIcon.layoutParams as ViewGroup.MarginLayoutParams
        profileIconLayoutParams.marginEnd = 4
        profileIcon.layoutParams = profileIconLayoutParams

        val textLayoutParams = textView.layoutParams as ViewGroup.MarginLayoutParams
        textLayoutParams.marginStart = 2
        textView.layoutParams = textLayoutParams

        return view
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_user_item, parent, false)
        val profileIcon: ImageView = view.findViewById(R.id.spinnerProfileIcon)
        val textView: TextView = view.findViewById(R.id.spinnerText)
        val settingsIcon: ImageView = view.findViewById(R.id.spinnerSettingsIcon)
        val divider: View = view.findViewById(R.id.spinnerDivider)

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

        // Log to ensure we're in the dropdown view creation
        Log.d("UserRoleAdapter", "Creating dropdown view for position: $position")

        settingsIcon.setOnClickListener {
            // Log to check if the settings icon is clicked
            Log.d("UserRoleAdapter", "Settings icon clicked for position: $position")

            // Temporary background change for visual feedback
            val originalBackground = view.background
            view.setBackgroundColor(context.getColor(android.R.color.darker_gray))
            view.postDelayed({
                view.background = originalBackground
            }, 200)

            // Call the external listener
            settingsIconClickListener?.invoke(position)

            // Safely access spinner, if it's not null
            spinner?.let {
                // Attempt to dismiss the dropdown using reflection (alternative to performing click)
                try {
                    val popupField = Spinner::class.java.getDeclaredField("mPopup")
                    popupField.isAccessible = true
                    val popupWindow = popupField.get(it) as? PopupWindow

                    // Check if the PopupWindow is showing and dismiss it
                    popupWindow?.let { window ->
                        if (window.isShowing) {
                            window.dismiss()
                            Log.d("UserRoleAdapter", "Dropdown closed successfully")
                        }
                    } ?: run {
                        Log.e("UserRoleAdapter", "PopupWindow is null or not accessible")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("UserRoleAdapter", "Error accessing PopupWindow: ${e.message}")
                }
            } ?: run {
                Log.e("UserRoleAdapter", "Spinner is null, cannot close dropdown")
            }
        }

        return view
    }
}
