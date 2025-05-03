package com.example.dressify

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.util.Log

class UserRoleAdapter(
    private val context: Context,
    private val roles: List<String>,
    private val iconResIds: List<Int>
) : BaseAdapter() {

    private var settingsIconClickListener: ((Int) -> Unit)? = null
    private var spinner: Spinner? = null

    fun setSettingsIconClickListener(listener: (Int) -> Unit) {
        settingsIconClickListener = listener
    }

    fun attachSpinner(spinner: Spinner) {
        this.spinner = spinner
    }

    override fun getCount(): Int = roles.size
    override fun getItem(position: Int): Any = roles[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, parent, isDropdown = false)
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, parent, isDropdown = true)
    }

    private fun createView(position: Int, parent: ViewGroup, isDropdown: Boolean): View {
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_user_item, parent, false)
        val profileIcon: ImageView = view.findViewById(R.id.spinnerProfileIcon)
        val textView: TextView = view.findViewById(R.id.spinnerText)
        val settingsIcon: ImageView = view.findViewById(R.id.spinnerSettingsIcon)
        val divider: View = view.findViewById(R.id.spinnerDivider)

        profileIcon.setImageResource(iconResIds[position])
        textView.text = roles[position]

        if (isDropdown) {
            profileIcon.layoutParams.height = 120
            profileIcon.layoutParams.width = 120
            profileIcon.scaleType = ImageView.ScaleType.CENTER_CROP

            textView.textSize = 18f
            textView.setPadding(8, 8, 8, 8)

            settingsIcon.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE

            settingsIcon.setOnClickListener {
                Log.d("UserRoleAdapter", "Settings icon clicked for position: $position")
                settingsIconClickListener?.invoke(position)
                closeDropdown()
            }
        } else {
            settingsIcon.visibility = View.GONE
            divider.visibility = View.GONE
        }

        return view
    }

    private fun closeDropdown() {
        spinner?.let {
            try {
                val popupField = Spinner::class.java.getDeclaredField("mPopup")
                popupField.isAccessible = true
                val popupWindow = popupField.get(it) as? PopupWindow
                popupWindow?.dismiss()
            } catch (e: Exception) {
                Log.e("UserRoleAdapter", "Error closing dropdown: ${e.message}")
            }
        }
    }
}