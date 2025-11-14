package com.shakti.ai.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.shakti.ai.R

data class FeatureCard(
    val title: String,
    val description: String,
    val backgroundColor: String,
    val iconResId: Int
)

class FeatureCardAdapter(
    private val features: List<FeatureCard>,
    private val onFeatureClick: (FeatureCard) -> Unit
) : RecyclerView.Adapter<FeatureCardAdapter.FeatureViewHolder>() {

    inner class FeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.card_feature)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_feature_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_feature_description)
        private val imgIcon: ImageView = itemView.findViewById(R.id.img_feature_icon)
        private val illustration: FrameLayout = itemView.findViewById(R.id.feature_illustration)

        fun bind(feature: FeatureCard) {
            tvTitle.text = feature.title
            tvDescription.text = feature.description
            card.setCardBackgroundColor(Color.parseColor(feature.backgroundColor))
            illustration.setBackgroundColor(Color.parseColor(feature.backgroundColor))

            // Set icon (using placeholder for now)
            imgIcon.setImageResource(feature.iconResId)

            itemView.setOnClickListener {
                onFeatureClick(feature)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature_card, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(features[position])
    }

    override fun getItemCount(): Int = features.size
}
