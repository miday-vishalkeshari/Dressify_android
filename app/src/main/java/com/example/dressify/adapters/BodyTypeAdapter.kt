package com.example.dressify.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.dressify.R

class BodyTypeAdapter(
    private val context: Context,
    private val bodyTypeImages: List<Int>
) : RecyclerView.Adapter<BodyTypeAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bodyImage: ImageView = itemView.findViewById(R.id.bodyTypeImageView)
        val bodyItemLayout: LinearLayout = itemView.findViewById(R.id.bodyTypeItemLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_body_type_image, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = bodyTypeImages.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bodyImage.setImageResource(bodyTypeImages[position])

        // Update selection state
        if (position == selectedPosition) {
            holder.bodyItemLayout.setBackgroundColor(context.getColor(android.R.color.holo_blue_light))
        } else {
            holder.bodyItemLayout.setBackgroundColor(context.getColor(android.R.color.transparent))
        }

        // OnClick listener for selection
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            // Return result immediately to RoleSettingActivity
            val resultIntent = Intent()
            resultIntent.putExtra("selectedBodyTypeIndex", selectedPosition)
            (context as Activity).setResult(Activity.RESULT_OK, resultIntent)
            (context as Activity).finish()
        }

    }

    fun getSelectedItem(): Int? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            bodyTypeImages[selectedPosition]
        } else null
    }
}