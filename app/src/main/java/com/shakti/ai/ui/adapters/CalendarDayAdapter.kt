package com.shakti.ai.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.shakti.ai.R
import com.shakti.ai.models.CalendarDay

class CalendarDayAdapter(
    private var days: List<CalendarDay>,
    private val onDayClick: (CalendarDay) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder>() {

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_day)
        private val tvDay: TextView = itemView.findViewById(R.id.tv_day)

        fun bind(day: CalendarDay) {
            tvDay.text = day.dayOfMonth.toString()

            // Reset defaults
            cardView.strokeWidth = 0

            // Set background color based on day type
            when {
                day.isToday -> {
                    cardView.setCardBackgroundColor(Color.parseColor("#E8F5FF"))
                    tvDay.setTextColor(Color.parseColor("#00ACC1"))
                    tvDay.textSize = 18f
                    tvDay.setTypeface(null, android.graphics.Typeface.BOLD)
                }

                day.isPeriodDay -> {
                    cardView.setCardBackgroundColor(Color.parseColor("#FFE6F0"))
                    tvDay.setTextColor(Color.parseColor("#FF6B9D"))
                    tvDay.textSize = 16f
                }
                day.isPredictedPeriod -> {
                    cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                    // Create dotted border effect via stroke
                    cardView.strokeWidth =
                        (4 * cardView.context.resources.displayMetrics.density).toInt()
                    cardView.strokeColor = Color.parseColor("#FF6B9D")
                    tvDay.setTextColor(Color.parseColor("#FF6B9D"))
                    tvDay.textSize = 16f
                }
                day.isFertileDay -> {
                    cardView.setCardBackgroundColor(Color.parseColor("#E8F8F5"))
                    tvDay.setTextColor(Color.parseColor("#00ACC1"))
                    tvDay.textSize = 16f
                }

                else -> {
                    cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                    tvDay.setTextColor(Color.parseColor("#78909C"))
                    tvDay.textSize = 16f
                    tvDay.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
            }

            // Add click listener
            itemView.setOnClickListener {
                onDayClick(day)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }
}
