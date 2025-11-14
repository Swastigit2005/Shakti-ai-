package com.shakti.ai.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.shakti.ai.R
import com.shakti.ai.models.*
import com.shakti.ai.ui.adapters.CalendarDayAdapter
import com.shakti.ai.ui.adapters.DailyInsightAdapter
import com.shakti.ai.viewmodel.SwasthyaViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SwasthyaAIFragment : Fragment() {

    private val viewModel: SwasthyaViewModel by activityViewModels()

    // UI Elements
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvPeriodPrediction: TextView
    private lateinit var tvDaysCount: TextView
    private lateinit var tvPregnancyChance: TextView
    private lateinit var btnLogPeriod: MaterialButton
    private lateinit var rvWeekCalendar: RecyclerView
    private lateinit var rvDailyInsights: RecyclerView

    // Adapters
    private lateinit var calendarAdapter: CalendarDayAdapter
    private lateinit var insightAdapter: DailyInsightAdapter

    // Data
    private var currentPeriodData: PeriodCycleData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_swasthya_ai, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeViews(view)
        setupCalendarRecyclerView()
        setupInsightsRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Initialize with sample data
        initializeSampleData()
    }

    private fun initializeViews(view: View) {
        tvCurrentDate = view.findViewById(R.id.tv_current_date)
        tvPeriodPrediction = view.findViewById(R.id.tv_period_prediction)
        tvDaysCount = view.findViewById(R.id.tv_days_count)
        tvPregnancyChance = view.findViewById(R.id.tv_pregnancy_chance)
        btnLogPeriod = view.findViewById(R.id.btn_log_period)
        rvWeekCalendar = view.findViewById(R.id.rv_week_calendar)
        rvDailyInsights = view.findViewById(R.id.rv_daily_insights)

        // Set current date
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMMM d")
        tvCurrentDate.text = today.format(formatter)

        // Close refer card
        view.findViewById<View>(R.id.btn_close_refer)?.setOnClickListener {
            view.findViewById<View>(R.id.card_refer)?.visibility = View.GONE
        }

        // Calendar full view
        view.findViewById<View>(R.id.btn_calendar_view)?.setOnClickListener {
            showFullCalendarDialog()
        }
    }

    private fun setupCalendarRecyclerView() {
        calendarAdapter = CalendarDayAdapter(emptyList()) { day ->
            onDayClicked(day)
        }
        rvWeekCalendar.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = calendarAdapter
        }
    }

    private fun setupInsightsRecyclerView() {
        insightAdapter = DailyInsightAdapter(emptyList()) { insight ->
            onInsightClicked(insight)
        }
        rvDailyInsights.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = insightAdapter
        }
    }

    private fun setupClickListeners() {
        btnLogPeriod.setOnClickListener {
            showLogPeriodDialog()
        }

        tvPregnancyChance.setOnClickListener {
            showPregnancyInfoDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cycleTracking.collect { tracking ->
                tracking?.let {
                    updatePeriodPrediction(it)
                }
            }
        }
    }

    private fun initializeSampleData() {
        // Create sample period data
        val today = LocalDate.now()
        val lastPeriod = today.minusDays(16)
        val nextPeriod = lastPeriod.plusDays(28)
        val daysUntil = ChronoUnit.DAYS.between(today, nextPeriod).toInt()

        currentPeriodData = PeriodCycleData(
            cycleDay = 17,
            cycleLength = 28,
            periodLength = 5,
            lastPeriodStart = lastPeriod,
            nextPeriodPrediction = nextPeriod,
            daysUntilNextPeriod = daysUntil,
            currentPhase = CyclePhase.OVULATION,
            ovulationDate = today,
            fertileWindowStart = today.minusDays(2),
            fertileWindowEnd = today.plusDays(1),
            pregnancyChance = PregnancyChance.HIGH
        )

        // Update UI with sample data
        updatePeriodPredictionUI(currentPeriodData!!)
        
        // Generate calendar days
        val calendarDays = generateWeekCalendarDays(today, currentPeriodData!!)
        calendarAdapter.updateDays(calendarDays)

        // Generate daily insights
        val insights = generateDailyInsights(currentPeriodData!!)
        insightAdapter.updateInsights(insights)
    }

    private fun updatePeriodPrediction(tracking: CycleTracking) {
        val daysUntil = tracking.daysUntilNextPeriod

        tvPeriodPrediction.text = "Period in"
        tvDaysCount.text = "$daysUntil days"

        // Update pregnancy chance text
        val phase = tracking.currentPhase
        when (phase) {
            CyclePhase.OVULATION -> {
                tvPregnancyChance.text = "See why pregnancy chances may be high ›"
            }
            CyclePhase.FOLLICULAR -> {
                tvPregnancyChance.text = "Pregnancy chances are lower ›"
            }
            else -> {
                tvPregnancyChance.text = "Lower chance of getting pregnant ⓘ"
            }
        }
    }

    private fun updatePeriodPredictionUI(data: PeriodCycleData) {
        tvPeriodPrediction.text = "Period in"
        tvDaysCount.text = "${data.daysUntilNextPeriod} days"

        when (data.pregnancyChance) {
            PregnancyChance.HIGH -> {
                tvPregnancyChance.text = "See why pregnancy chances may be high ›"
            }
            PregnancyChance.MEDIUM -> {
                tvPregnancyChance.text = "Pregnancy chances are moderate ›"
            }
            else -> {
                tvPregnancyChance.text = "Lower chance of getting pregnant ⓘ"
            }
        }
    }

    private fun generateWeekCalendarDays(today: LocalDate, data: PeriodCycleData): List<CalendarDay> {
        val days = mutableListOf<CalendarDay>()
        val startDate = today.minusDays(3)

        for (i in 0..6) {
            val date = startDate.plusDays(i.toLong())
            val isToday = date == today
            val isPeriodDay = date >= data.lastPeriodStart && 
                             date < data.lastPeriodStart.plusDays(data.periodLength.toLong())
            val isPredictedPeriod = date >= data.nextPeriodPrediction && 
                                   date < data.nextPeriodPrediction.plusDays(data.periodLength.toLong())
            val isFertileDay = data.fertileWindowStart != null && data.fertileWindowEnd != null &&
                              date >= data.fertileWindowStart && date <= data.fertileWindowEnd

            days.add(
                CalendarDay(
                    date = date,
                    dayOfMonth = date.dayOfMonth,
                    dayOfWeek = date.dayOfWeek.name.substring(0, 1),
                    isToday = isToday,
                    isPeriodDay = isPeriodDay,
                    isFertileDay = isFertileDay,
                    isPredictedPeriod = isPredictedPeriod,
                    hasSymptoms = false
                )
            )
        }

        return days
    }

    private fun generateDailyInsights(data: PeriodCycleData): List<DailyInsightCard> {
        val insights = mutableListOf<DailyInsightCard>()

        // Log Symptoms Card (with plus icon)
        insights.add(
            DailyInsightCard(
                id = "log_symptoms",
                type = InsightType.LOG_SYMPTOMS,
                title = "Log your\nsymptoms",
                description = "",
                actionText = "+",
                backgroundColor = "#E8F5FF"
            )
        )

        // Pregnancy Chance Card
        val pregnancyText = when (data.pregnancyChance) {
            PregnancyChance.HIGH -> "High"
            PregnancyChance.MEDIUM -> "Medium"
            PregnancyChance.LOW -> "Low"
            PregnancyChance.NONE -> "Very Low"
        }
        insights.add(
            DailyInsightCard(
                id = "pregnancy_chance",
                type = InsightType.PREGNANCY_CHANCE,
                title = "Today's\nchance of\npregnancy",
                description = pregnancyText,
                actionText = "See update",
                backgroundColor = "#C5E3F6"
            )
        )

        // Cycle Day Card
        insights.add(
            DailyInsightCard(
                id = "cycle_day",
                type = InsightType.CYCLE_DAY,
                title = "Cycle day",
                description = "${data.cycleDay}",
                actionText = null,
                backgroundColor = "#E6D7FF"
            )
        )

        // Symptom Forecast Card
        insights.add(
            DailyInsightCard(
                id = "symptom_forecast",
                type = InsightType.SYMPTOM_FORECAST,
                title = "${LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM d"))}:\nSymptoms to\nexpect",
                description = "",
                actionText = "Show forecast",
                backgroundColor = "#B2DFDB"
            )
        )

        return insights
    }

    private fun onDayClicked(day: CalendarDay) {
        // Show day details and allow symptom logging
        showDayDetailsDialog(day)
    }

    private fun onInsightClicked(insight: DailyInsightCard) {
        when (insight.type) {
            InsightType.LOG_SYMPTOMS -> showSymptomLoggerDialog()
            InsightType.PREGNANCY_CHANCE -> showPregnancyInfoDialog()
            InsightType.CYCLE_DAY -> showCycleDayInfo()
            InsightType.SYMPTOM_FORECAST -> showSymptomForecast()
            else -> {
                Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSymptomLoggerDialog() {
        // TODO: Implement symptom selection UI with custom dialog
        Toast.makeText(
            context,
            "📝 Symptom Logger\n\nClick + icon to add today's symptoms:\n• Cramps\n• Mood swings\n• Headache\n• Bloating\nand more...",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showLogPeriodDialog() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Log Period")
            .setMessage("Mark today as the first day of your period?")
            .setPositiveButton("Yes") { _, _ ->
                val today = LocalDate.now()
                viewModel.trackMenstrualCycle(
                    lastPeriodDate = today,
                    cycleLength = 28,
                    periodDuration = 5
                )
                Toast.makeText(context, "✓ Period logged successfully!", Toast.LENGTH_SHORT).show()
                
                // Refresh data
                initializeSampleData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPregnancyInfoDialog() {
        val message = when (currentPeriodData?.currentPhase) {
            CyclePhase.OVULATION -> {
                """
                🔮 High Pregnancy Chance
                
                You're currently in your ovulation phase (day ${currentPeriodData?.cycleDay}), which means:
                
                • Peak fertility window
                • Ovulation likely today or within 24 hours
                • Highest chance of conception
                • Body temperature may be slightly higher
                • Increased cervical mucus
                
                If you're trying to conceive, this is the optimal time!
                If you're preventing pregnancy, use protection.
                """.trimIndent()
            }
            CyclePhase.FOLLICULAR -> {
                """
                📊 Lower Pregnancy Chance
                
                You're in the follicular phase (day ${currentPeriodData?.cycleDay}):
                
                • Lower fertility
                • Body preparing for ovulation
                • Pregnancy still possible but less likely
                """.trimIndent()
            }
            else -> {
                """
                📊 Pregnancy Chances
                
                Currently on day ${currentPeriodData?.cycleDay} of your cycle.
                
                Your pregnancy chance varies throughout your cycle:
                • Highest during ovulation (days 12-16)
                • Lower during menstruation and late luteal phase
                """.trimIndent()
            }
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Pregnancy Information")
            .setMessage(message)
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showDayDetailsDialog(day: CalendarDay) {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
        val dateStr = day.date.format(formatter)
        
        val status = when {
            day.isPeriodDay -> "🩸 Period Day"
            day.isPredictedPeriod -> "📅 Predicted Period"
            day.isFertileDay -> "💚 Fertile Window"
            day.isToday -> "📍 Today"
            else -> "Regular Day"
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(dateStr)
            .setMessage("$status\n\nWould you like to log symptoms for this day?")
            .setPositiveButton("Log Symptoms") { _, _ ->
                showSymptomLoggerDialog()
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showCycleDayInfo() {
        val data = currentPeriodData ?: return
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Cycle Day ${data.cycleDay}")
            .setMessage(
                """
                Current Phase: ${data.currentPhase.name}
                
                Your cycle breakdown:
                • Menstruation: Days 1-${data.periodLength}
                • Follicular: Days ${data.periodLength + 1}-13
                • Ovulation: Days 14-16
                • Luteal: Days 17-${data.cycleLength}
                
                Next Period: ${data.nextPeriodPrediction.format(DateTimeFormatter.ofPattern("MMMM d"))}
                """.trimIndent()
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSymptomForecast() {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMMM d")
        
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("AI Symptom Forecast")
            .setMessage(
                """
                Based on your cycle patterns, you may experience:
                
                🌡️ ${today.format(formatter)}:
                • Increased energy
                • Clear skin
                • Heightened libido
                
                📅 ${today.plusDays(7).format(formatter)} - ${today.plusDays(10).format(formatter)}:
                • Possible PMS symptoms
                • Mood changes
                • Breast tenderness
                • Bloating
                
                💡 AI Tip: Track your symptoms daily for more accurate predictions!
                """.trimIndent()
            )
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showFullCalendarDialog() {
        // TODO: Implement full month calendar view
        Toast.makeText(context, "Full Calendar View - Coming Soon", Toast.LENGTH_SHORT).show()
    }
}
