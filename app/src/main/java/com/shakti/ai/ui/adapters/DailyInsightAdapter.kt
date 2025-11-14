package com.shakti.ai.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.shakti.ai.R
import com.shakti.ai.models.DailyInsightCard

class DailyInsightAdapter(
    private var insights: List<DailyInsightCard>,
    private val onInsightClick: (DailyInsightCard) -> Unit
) : RecyclerView.Adapter<DailyInsightAdapter.InsightViewHolder>() {

    inner class InsightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_insight)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_insight_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_insight_description)
        private val tvAction: TextView = itemView.findViewById(R.id.tv_insight_action)
        private val imgIcon: ImageView = itemView.findViewById(R.id.img_insight_icon)

        fun bind(insight: DailyInsightCard) {
            tvTitle.text = insight.title
            tvDescription.text = insight.description

            // Set action text or hide if null
            if (insight.actionText != null) {
                tvAction.text = insight.actionText
                tvAction.visibility = View.VISIBLE
            } else {
                tvAction.visibility = View.GONE
            }

            // Set background color
            cardView.setCardBackgroundColor(Color.parseColor(insight.backgroundColor))

            // Set icon if provided
            if (insight.iconResId != null) {
                imgIcon.setImageResource(insight.iconResId)
                imgIcon.visibility = View.VISIBLE
            } else {
                imgIcon.visibility = View.GONE
            }

            // Click listener
            itemView.setOnClickListener {
                onInsightClick(insight)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_insight, parent, false)
        return InsightViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsightViewHolder, position: Int) {
        holder.bind(insights[position])
    }

    override fun getItemCount(): Int = insights.size

    fun updateInsights(newInsights: List<DailyInsightCard>) {
        insights = newInsights
        notifyDataSetChanged()
    }
}
