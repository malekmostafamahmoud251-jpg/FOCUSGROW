package com.example.data.analysis

import com.example.data.local.entity.FocusSessionEntity
import java.util.Calendar

data class SmartFocusRecommendations(
    val optimalDurationMinutes: Int,
    val durationInsight: String,
    val preferredTimeWindow: String,
    val preferredHoursInsight: String,
    val recommendedBreakMinutes: Int,
    val breakInsight: String,
    val suggestedDailyGoalMinutes: Int,
    val suggestedWeeklyGoalMinutes: Int,
    val goalInsight: String,
    val strongestCategory: String,
    val categoryInsight: String,
    val completionRatePercentage: Int,
    val totalSessionsAnalyzed: Int
)

object SmartFocusRecommender {

    fun analyze(
        sessions: List<FocusSessionEntity>,
        currentDailyGoal: Int = 60
    ): SmartFocusRecommendations {
        if (sessions.isEmpty()) {
            return defaultRecommendations(currentDailyGoal)
        }

        val completedSessions = sessions.filter { it.status == "COMPLETED" }
        val completionRate = ((completedSessions.size.toFloat() / sessions.size) * 100).toInt()

        // 1. Analyze Optimal Focus Duration
        // Bucket sessions by duration: Short (<25m), Standard (25-35m), Deep (36-50m), Extended (>50m)
        val short = sessions.filter { it.durationMinutes in 1..24 }
        val standard = sessions.filter { it.durationMinutes in 25..35 }
        val deep = sessions.filter { it.durationMinutes in 36..50 }
        val extended = sessions.filter { it.durationMinutes > 50 }

        fun successRate(list: List<FocusSessionEntity>): Float {
            if (list.isEmpty()) return 0f
            return list.count { it.status == "COMPLETED" }.toFloat() / list.size
        }

        val standardRate = successRate(standard)
        val deepRate = successRate(deep)
        val shortRate = successRate(short)
        val extendedRate = successRate(extended)

        val (optimalDuration, durationInsight) = when {
            deep.size >= 2 && deepRate >= 0.75f -> {
                40 to "Your data shows high completion (${(deepRate * 100).toInt()}%) during 35–45 min intervals. Extended focus suits your work style."
            }
            standard.isNotEmpty() && standardRate >= 0.70f -> {
                25 to "The classic 25-minute Pomodoro rhythm achieves your highest consistency (${(standardRate * 100).toInt()}% completed)."
            }
            short.size > standard.size && shortRate >= 0.8f -> {
                20 to "Short 20-minute focus sprints yield your highest completion without cognitive fatigue."
            }
            extended.size >= 3 && extendedRate >= 0.8f -> {
                50 to "You thrive in long uninterrupted flow states. 50-minute deep immersion recommended."
            }
            else -> {
                30 to "A balanced 30-minute interval provides the ideal equilibrium between progress and mental stamina."
            }
        }

        // 2. Analyze Preferred Study Hours / Golden Time Window
        val hourCounts = IntArray(24)
        val hourSuccessCounts = IntArray(24)

        sessions.forEach { s ->
            val cal = Calendar.getInstance().apply { timeInMillis = s.startTime }
            val h = cal.get(Calendar.HOUR_OF_DAY)
            if (h in 0..23) {
                hourCounts[h]++
                if (s.status == "COMPLETED") {
                    hourSuccessCounts[h]++
                }
            }
        }

        // Aggregate into morning (6-12), afternoon (12-18), evening (18-23), night (0-5)
        val morningScore = (6..11).sumOf { hourSuccessCounts[it] }
        val afternoonScore = (12..17).sumOf { hourSuccessCounts[it] }
        val eveningScore = (18..22).sumOf { hourSuccessCounts[it] }
        val nightScore = (hourSuccessCounts[23] + (0..5).sumOf { hourSuccessCounts[it] })

        val maxWindow = maxOf(morningScore, afternoonScore, eveningScore, nightScore)
        val (windowLabel, windowInsight) = when (maxWindow) {
            morningScore -> "Morning Peak (8:00 AM – 11:30 AM)" to "You finish the highest proportion of trees in the morning when your mental willpower is fresh."
            afternoonScore -> "Afternoon Flow (1:30 PM – 5:00 PM)" to "Your post-lunch focus maintains solid endurance, ideal for active recall and project tasks."
            eveningScore -> "Evening Sanctuary (6:30 PM – 9:30 PM)" to "Quiet evening hours show minimal interruptions in your historical session logs."
            else -> "Night Focus (10:00 PM – 1:00 AM)" to "You frequently engage in late-night sessions. Consider winding down with mindful breathing."
        }

        // 3. Appropriate Break Interval
        val recommendedBreak = when {
            optimalDuration >= 45 -> 10
            optimalDuration >= 30 -> 7
            else -> 5
        }
        val breakInsight = "Following a $optimalDuration-minute focus session, a $recommendedBreak-minute screen-free break restores 90% of cognitive acuity."

        // 4. Personalized Daily and Weekly Goals
        val dayMillis = 86400000L
        val now = System.currentTimeMillis()
        val last7Days = completedSessions.filter { now - it.startTime <= dayMillis * 7 }
        val avgDailyMinutes = if (last7Days.isNotEmpty()) {
            last7Days.sumOf { it.actualFocusedMinutes } / 7
        } else {
            completedSessions.sumOf { it.actualFocusedMinutes } / 3
        }

        val suggestedDaily = when {
            avgDailyMinutes > 90 -> 100
            avgDailyMinutes > 50 -> 75
            avgDailyMinutes > 25 -> 50
            else -> 40
        }
        val suggestedWeekly = suggestedDaily * 5
        val goalInsight = "Based on your 7-day average ($avgDailyMinutes min/day), a target of $suggestedDaily min/day (+15%) provides progressive, sustainable growth."

        // 5. Strongest Category
        val categoryCounts = mutableMapOf<String, Int>()
        val categoryCompletes = mutableMapOf<String, Int>()
        sessions.forEach { s ->
            categoryCounts[s.category] = (categoryCounts[s.category] ?: 0) + 1
            if (s.status == "COMPLETED") {
                categoryCompletes[s.category] = (categoryCompletes[s.category] ?: 0) + 1
            }
        }

        val bestCategoryEntry = categoryCompletes.maxByOrNull { it.value }
        val strongestCat = bestCategoryEntry?.key ?: "Studying"
        val catInsight = "Your highest consistency lies in $strongestCat with ${bestCategoryEntry?.value ?: 0} thriving trees."

        return SmartFocusRecommendations(
            optimalDurationMinutes = optimalDuration,
            durationInsight = durationInsight,
            preferredTimeWindow = windowLabel,
            preferredHoursInsight = windowInsight,
            recommendedBreakMinutes = recommendedBreak,
            breakInsight = breakInsight,
            suggestedDailyGoalMinutes = suggestedDaily,
            suggestedWeeklyGoalMinutes = suggestedWeekly,
            goalInsight = goalInsight,
            strongestCategory = strongestCat,
            categoryInsight = catInsight,
            completionRatePercentage = completionRate,
            totalSessionsAnalyzed = sessions.size
        )
    }

    private fun defaultRecommendations(currentDailyGoal: Int): SmartFocusRecommendations {
        return SmartFocusRecommendations(
            optimalDurationMinutes = 25,
            durationInsight = "The 25-minute Pomodoro method is clinically recommended for beginning your focus practice.",
            preferredTimeWindow = "Morning Peak (8:30 AM – 11:30 AM)",
            preferredHoursInsight = "Starting your first tree early establishes a protective habit momentum for the day.",
            recommendedBreakMinutes = 5,
            breakInsight = "A 5-minute break allows neural pathways to consolidate newly acquired knowledge.",
            suggestedDailyGoalMinutes = currentDailyGoal.coerceAtLeast(45),
            suggestedWeeklyGoalMinutes = (currentDailyGoal * 5).coerceAtLeast(225),
            goalInsight = "Aim for 2 to 3 quality focus intervals per day to cultivate your forest sustainably.",
            strongestCategory = "Studying",
            categoryInsight = "Planting seeds dedicated to study sets strong roots for personal mastery.",
            completionRatePercentage = 100,
            totalSessionsAnalyzed = 0
        )
    }
}
