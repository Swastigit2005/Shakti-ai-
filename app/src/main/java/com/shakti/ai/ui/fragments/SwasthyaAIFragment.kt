package com.shakti.ai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.shakti.ai.R
import com.shakti.ai.models.HealthCategory
import com.shakti.ai.viewmodel.SwasthyaViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SwasthyaAIFragment : Fragment() {

    private val viewModel: SwasthyaViewModel by activityViewModels()

    // UI Elements
    private lateinit var btnHeavyBleeding: Button
    private lateinit var btnMoodSwings: Button
    private lateinit var btnSevereCramps: Button
    private lateinit var btnLogPeriodDay: Button
    private lateinit var btnBookConsultation: Button
    private lateinit var btnPeriodTracker: Button
    private lateinit var btnHealthTips: Button
    private lateinit var btnSymptomChecker: Button
    private lateinit var cycleInfoText: TextView

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
        setupClickListeners()
        observeViewModel()

        // Initialize with demo data
        viewModel.trackMenstrualCycle(
            lastPeriodDate = LocalDate.now().minusDays(15),
            cycleLength = 28,
            periodDuration = 5
        )
    }

    private fun initializeViews(view: View) {
        btnHeavyBleeding = view.findViewById(R.id.btn_heavy_bleeding)
        btnMoodSwings = view.findViewById(R.id.btn_mood_swings)
        btnSevereCramps = view.findViewById(R.id.btn_severe_cramps)
        btnLogPeriodDay = view.findViewById(R.id.btn_log_period_day)
        btnBookConsultation = view.findViewById(R.id.btn_book_consultation)

        // Additional buttons
        btnPeriodTracker = createButton("📅 Period Tracker")
        btnHealthTips = createButton("💡 Health Tips")
        btnSymptomChecker = createButton("🔍 Symptom Checker")

        // Create cycle info text view
        cycleInfoText = TextView(requireContext())
    }

    private fun createButton(text: String): Button {
        return Button(requireContext()).apply {
            this.text = text
        }
    }

    private fun setupClickListeners() {
        btnHeavyBleeding.setOnClickListener {
            logSymptom("Heavy Bleeding - soaking through pad/tampon every 1-2 hours")
        }

        btnMoodSwings.setOnClickListener {
            logSymptom("Mood Swings - sudden emotional changes, irritability, or sadness")
        }

        btnSevereCramps.setOnClickListener {
            logSymptom("Severe Cramps - intense pelvic pain, difficulty in daily activities")
        }

        btnLogPeriodDay.setOnClickListener {
            logPeriodDay()
        }

        btnBookConsultation.setOnClickListener {
            bookConsultation()
        }

        btnPeriodTracker.setOnClickListener {
            showPeriodTrackerDialog()
        }

        btnHealthTips.setOnClickListener {
            showHealthTips()
        }

        btnSymptomChecker.setOnClickListener {
            showSymptomCheckerDialog()
        }
    }

    private fun observeViewModel() {
        // Observe cycle tracking
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cycleTracking.collect { tracking ->
                tracking?.let {
                    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
                    val info = """
                        Next Period: ${it.nextPeriod.format(dateFormatter)}
                        Current Phase: ${it.currentPhase.name}
                        Ovulation Date: ${it.ovulationDate.format(dateFormatter)}
                        Days Until Next Period: ${it.daysUntilNextPeriod}
                        
                        Health Tips for ${it.currentPhase.name} Phase:
                        ${it.healthTips.joinToString("\n") { tip -> "• $tip" }}
                    """.trimIndent()

                    cycleInfoText.text = info
                }
            }
        }

        // Observe symptom analysis
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.symptomAnalysis.collect { analysis ->
                analysis?.let {
                    showSymptomAnalysisDialog(
                        it.symptoms,
                        it.aiAdvice,
                        it.urgencyLevel.name,
                        it.emergencyWarning,
                        it.whenToSeeDoctor,
                        it.homeRemedies
                    )
                }
            }
        }

        // Observe wellness tips
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.wellnessTips.collect { tips ->
                if (tips.isNotEmpty()) {
                    // Tips are displayed when user requests them
                }
            }
        }

        // Observe loading state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                btnHeavyBleeding.isEnabled = !isLoading
                btnMoodSwings.isEnabled = !isLoading
                btnSevereCramps.isEnabled = !isLoading
                btnLogPeriodDay.isEnabled = !isLoading
                btnSymptomChecker.isEnabled = !isLoading
            }
        }

        // Observe error messages
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun logSymptom(symptom: String) {
        Toast.makeText(context, "Logging symptom...", Toast.LENGTH_SHORT).show()
        viewModel.checkSymptoms(symptom)
    }

    private fun logPeriodDay() {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Log Period Day")
            .setMessage("Mark today as day 1 of your period?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.trackMenstrualCycle(
                    lastPeriodDate = LocalDate.now(),
                    cycleLength = 28,
                    periodDuration = 5
                )
                Toast.makeText(context, "Period day logged!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPeriodTrackerDialog() {
        val view = layoutInflater.inflate(
            android.R.layout.simple_list_item_1,
            null
        ) as android.widget.LinearLayout
        view.orientation = android.widget.LinearLayout.VERTICAL
        view.setPadding(50, 20, 50, 20)

        val lastPeriodInput = EditText(requireContext()).apply {
            hint = "Days since last period (e.g., 15)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val cycleLengthInput = EditText(requireContext()).apply {
            hint = "Cycle length (usually 28 days)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("28")
        }
        val periodDurationInput = EditText(requireContext()).apply {
            hint = "Period duration (usually 5 days)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("5")
        }

        view.addView(lastPeriodInput)
        view.addView(cycleLengthInput)
        view.addView(periodDurationInput)

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Update Period Tracker")
            .setMessage("Enter your cycle details:")
            .setView(view)
            .setPositiveButton("Track") { _, _ ->
                val daysSince = lastPeriodInput.text.toString().toIntOrNull() ?: 15
                val cycleLength = cycleLengthInput.text.toString().toIntOrNull() ?: 28
                val duration = periodDurationInput.text.toString().toIntOrNull() ?: 5

                val lastPeriod = LocalDate.now().minusDays(daysSince.toLong())
                viewModel.trackMenstrualCycle(lastPeriod, cycleLength, duration)

                Toast.makeText(context, "Cycle tracker updated!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHealthTips() {
        viewModel.getWellnessTips(HealthCategory.PHYSICAL_FITNESS)

        viewLifecycleOwner.lifecycleScope.launch {
            val tips = viewModel.wellnessTips.value

            val tipsText = if (tips.isNotEmpty()) {
                tips.joinToString("\n\n") { " $it" }
            } else {
                """
                General Health Tips for Women:
                
                Exercise regularly (30 min/day)
                Eat balanced diet with iron & calcium
                Drink 8 glasses of water daily
                Get 7-8 hours of sleep
                Practice stress management
                Regular health checkups
                Avoid smoking and excessive alcohol
                Track menstrual cycle
                
                Stay healthy, stay strong!
            """.trimIndent()
            }

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Health & Wellness Tips")
                .setMessage(tipsText)
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun showSymptomCheckerDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Describe your symptoms..."
            minLines = 3
            setPadding(50, 40, 50, 40)
        }

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("AI Symptom Checker")
            .setMessage("Describe what you're experiencing:")
            .setView(input)
            .setPositiveButton("Analyze") { _, _ ->
                val symptoms = input.text.toString()
                if (symptoms.isNotBlank()) {
                    viewModel.checkSymptoms(symptoms)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun bookConsultation() {
        val doctors = arrayOf(
            "Dr. Priya Sharma - Gynecologist (10+ years)\n 4.9 • ₹299",
            "Dr. Anjali Mehta - Gynecologist (8+ years)\n 4.8 • ₹249",
            "Dr. Kavita Singh - Obstetrics (12+ years)\n 5.0 • ₹349",
            "Dr. Rekha Gupta - Women's Health (15+ years)\n 4.9 • ₹399"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Book Telemedicine Consultation")
            .setMessage("Connect with female doctors via video call.\n\n100% Private & Confidential")
            .setItems(doctors) { _, which ->
                val doctor = doctors[which].split("-")[0].trim()
                Toast.makeText(
                    context,
                    "Consultation booked with $doctor\nNext slot: Today, 6:00 PM",
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSymptomAnalysisDialog(
        symptoms: String,
        advice: String,
        urgency: String,
        emergency: Boolean,
        whenToSeeDoctor: String,
        homeRemedies: List<String>
    ) {
        val urgencyIcon = when (urgency) {
            "LOW" -> ""
            "MEDIUM" -> ""
            "HIGH" -> ""
            "EMERGENCY" -> ""
            else -> ""
        }

        val title =
            if (emergency) " EMERGENCY - Seek Immediate Help" else "$urgencyIcon Health Advice"

        val remediesText = if (homeRemedies.isNotEmpty()) {
            "\n\n Home Remedies:\n${homeRemedies.joinToString("\n") { " $it" }}"
        } else ""

        val message = """
            Symptoms: $symptoms
            
            Urgency Level: $urgency
            
            AI Advice:
            $advice
            
            When to See Doctor:
            $whenToSeeDoctor
            $remediesText
            
            ${if (emergency) "\n PLEASE SEEK IMMEDIATE MEDICAL ATTENTION!" else ""}
        """.trimIndent()

        android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(if (emergency) "Call Emergency" else "Book Doctor") { _, _ ->
                if (emergency) {
                    // Call emergency number
                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                        data = android.net.Uri.parse("tel:112")
                    }
                    startActivity(intent)
                } else {
                    bookConsultation()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
