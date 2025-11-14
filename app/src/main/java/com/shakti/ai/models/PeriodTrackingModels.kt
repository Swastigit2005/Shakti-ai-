package com.shakti.ai.models

import java.time.LocalDate

/**
 * Period Tracking Data Models for Swasthya AI Dashboard
 */

// Calendar Day Model
data class CalendarDay(
    val date: LocalDate,
    val dayOfMonth: Int,
    val dayOfWeek: String,
    val isToday: Boolean = false,
    val isPeriodDay: Boolean = false,
    val isFertileDay: Boolean = false,
    val isPredictedPeriod: Boolean = false,
    val hasSymptoms: Boolean = false
)

// Period Cycle Data
data class PeriodCycleData(
    val cycleDay: Int,
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val lastPeriodStart: LocalDate,
    val nextPeriodPrediction: LocalDate,
    val daysUntilNextPeriod: Int,
    val currentPhase: CyclePhase,
    val ovulationDate: LocalDate?,
    val fertileWindowStart: LocalDate?,
    val fertileWindowEnd: LocalDate?,
    val pregnancyChance: PregnancyChance = PregnancyChance.LOW
)

// Pregnancy Chance
enum class PregnancyChance {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}

// Daily Symptom
data class DailySymptom(
    val date: LocalDate,
    val symptoms: List<SymptomType>,
    val mood: MoodType?,
    val flowLevel: FlowLevel?,
    val painLevel: Int = 0, // 0-10 scale
    val notes: String = ""
)

// Symptom Types
enum class SymptomType {
    CRAMPS,
    HEADACHE,
    BLOATING,
    BREAST_TENDERNESS,
    FATIGUE,
    NAUSEA,
    ACNE,
    BACK_PAIN,
    MOOD_SWINGS,
    ANXIETY,
    IRRITABILITY,
    FOOD_CRAVINGS,
    INSOMNIA,
    HOT_FLASHES,
    SPOTTING
}

// Mood Types
enum class MoodType {
    HAPPY,
    SAD,
    ANXIOUS,
    IRRITABLE,
    CALM,
    ENERGETIC,
    TIRED,
    STRESSED
}

// Flow Level
enum class FlowLevel {
    SPOTTING,
    LIGHT,
    MEDIUM,
    HEAVY,
    VERY_HEAVY
}

// Daily Insight Card
data class DailyInsightCard(
    val id: String,
    val type: InsightType,
    val title: String,
    val description: String,
    val actionText: String? = null,
    val backgroundColor: String,
    val iconResId: Int? = null
)

// Insight Types
enum class InsightType {
    LOG_SYMPTOMS,
    PREGNANCY_CHANCE,
    CYCLE_DAY,
    MOOD_PREDICTION,
    QUIZ,
    HEALTH_TIP,
    SYMPTOM_FORECAST,
    OVULATION_ALERT,
    PMS_WARNING
}

// AI Prediction
data class AIPrediction(
    val nextPeriodDate: LocalDate,
    val confidence: Float, // 0.0 to 1.0
    val moodSwingsPrediction: List<MoodPrediction>,
    val symptomsPrediction: List<SymptomPrediction>,
    val ovulationPrediction: OvulationPrediction?
)

// Mood Prediction
data class MoodPrediction(
    val date: LocalDate,
    val mood: MoodType,
    val confidence: Float
)

// Symptom Prediction
data class SymptomPrediction(
    val date: LocalDate,
    val symptom: SymptomType,
    val likelihood: Float // 0.0 to 1.0
)

// Ovulation Prediction
data class OvulationPrediction(
    val date: LocalDate,
    val fertileWindowStart: LocalDate,
    val fertileWindowEnd: LocalDate,
    val confidence: Float
)

// Period Log Entry
data class PeriodLogEntry(
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val cycleLength: Int,
    val flowIntensity: FlowLevel,
    val symptoms: List<SymptomType>,
    val notes: String = ""
)

// Health Alert
data class HealthAlert(
    val id: String,
    val type: HealthAlertType,
    val title: String,
    val message: String,
    val severity: AlertSeverity,
    val actionRequired: Boolean = false,
    val actionLabel: String? = null
)

// Health Alert Types
enum class HealthAlertType {
    IRREGULAR_CYCLE,
    HEAVY_BLEEDING,
    SEVERE_PAIN,
    MISSED_PERIOD,
    ABNORMAL_SYMPTOMS,
    FERTILITY_WINDOW,
    PMS_REMINDER,
    CONSULTATION_NEEDED
}

// Statistics
data class CycleStatistics(
    val averageCycleLength: Double,
    val cycleRegularity: CycleRegularity,
    val averageFlowDays: Double,
    val commonSymptoms: List<SymptomType>,
    val moodPatterns: Map<CyclePhase, List<MoodType>>
)

// Cycle Regularity
enum class CycleRegularity {
    REGULAR,
    SOMEWHAT_REGULAR,
    IRREGULAR,
    VERY_IRREGULAR,
    UNKNOWN
}
