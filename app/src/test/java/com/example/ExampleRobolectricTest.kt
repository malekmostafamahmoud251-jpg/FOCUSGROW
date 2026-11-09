package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.analysis.SmartFocusRecommender
import com.example.data.local.entity.FocusSessionEntity
import com.example.data.model.TreeGrowthStage
import com.example.data.model.ALL_TREE_SPECIES
import com.example.ui.components.ForestWeatherType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Evergreen app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Evergreen", appName)
    }

    @Test
    fun `tree growth stages transition accurately`() {
        assertEquals(TreeGrowthStage.SEED, TreeGrowthStage.fromProgress(0.0f))
        assertEquals(TreeGrowthStage.SPROUT, TreeGrowthStage.fromProgress(0.2f))
        assertEquals(TreeGrowthStage.SAPLING, TreeGrowthStage.fromProgress(0.5f))
        assertEquals(TreeGrowthStage.YOUNG_TREE, TreeGrowthStage.fromProgress(0.8f))
        assertEquals(TreeGrowthStage.MATURE_TREE, TreeGrowthStage.fromProgress(1.0f))
    }

    @Test
    fun `default tree species catalog contains essentials and new botanical additions`() {
        assertEquals(16, ALL_TREE_SPECIES.size)
        val pine = ALL_TREE_SPECIES.find { it.id == "classic_pine" }
        assertEquals("Classic Pine", pine?.name)
        assertEquals(0, pine?.price)

        val baobab = ALL_TREE_SPECIES.find { it.id == "ancient_baobab" }
        assertEquals("African Baobab", baobab?.name)

        val jacaranda = ALL_TREE_SPECIES.find { it.id == "jacaranda_bloom" }
        assertEquals("Jacaranda Bloom", jacaranda?.name)

        val wisteria = ALL_TREE_SPECIES.find { it.id == "moonlit_wisteria" }
        assertEquals("Moonlit Wisteria", wisteria?.name)

        val crystal = ALL_TREE_SPECIES.find { it.id == "prismatic_crystal" }
        assertEquals("Crystal Prism", crystal?.name)

        val bamboo = ALL_TREE_SPECIES.find { it.id == "zen_bamboo" }
        assertEquals("Zen Bamboo", bamboo?.name)
    }

    @Test
    fun `study goal presets have valid titles and pacing strategies`() {
        assertEquals(6, com.example.ui.screens.ai.STUDY_GOAL_PRESETS.size)
        com.example.ui.screens.ai.STUDY_GOAL_PRESETS.forEach { preset ->
            assertTrue(preset.title.isNotBlank())
            assertTrue(preset.goalPrompt.isNotBlank())
            assertTrue(preset.defaultPacing.focusMinutes > 0)
            assertTrue(preset.defaultPacing.breakMinutes > 0)
        }
    }

    @Test
    fun `smart focus recommendations compute realistic intervals`() {
        // Test empty history fallback
        val defaultRec = SmartFocusRecommender.analyze(emptyList(), 60)
        assertEquals(25, defaultRec.optimalDurationMinutes)
        assertEquals(5, defaultRec.recommendedBreakMinutes)
        assertTrue(defaultRec.suggestedDailyGoalMinutes >= 45)

        // Test with historical sessions
        val now = System.currentTimeMillis()
        val sampleSessions = listOf(
            FocusSessionEntity(
                startTime = now - 7200000L,
                endTime = now - 7200000L + 2400000L,
                durationMinutes = 40,
                actualFocusedMinutes = 40,
                status = "COMPLETED",
                category = "Studying",
                treeSpeciesId = "classic_pine",
                coinsEarned = 20,
                xpEarned = 80
            ),
            FocusSessionEntity(
                startTime = now - 3600000L,
                endTime = now - 3600000L + 2400000L,
                durationMinutes = 40,
                actualFocusedMinutes = 40,
                status = "COMPLETED",
                category = "Studying",
                treeSpeciesId = "ancient_oak",
                coinsEarned = 20,
                xpEarned = 80
            )
        )
        val analyzed = SmartFocusRecommender.analyze(sampleSessions, 60)
        assertEquals(40, analyzed.optimalDurationMinutes)
        assertEquals(7, analyzed.recommendedBreakMinutes)
        assertEquals(100, analyzed.completionRatePercentage)
        assertEquals("Studying", analyzed.strongestCategory)
    }

    @Test
    fun `forest weather resolves accurately based on streak and time of day`() {
        // Nighttime hours trigger starry twilight
        assertEquals(ForestWeatherType.STARRY_TWILIGHT, ForestWeatherType.resolveFromActivity(streak = 5, hourOfDay = 22))
        assertEquals(ForestWeatherType.STARRY_TWILIGHT, ForestWeatherType.resolveFromActivity(streak = 1, hourOfDay = 3))

        // High streak (3+ days) triggers radiant golden sunbeams during daytime
        assertEquals(ForestWeatherType.GOLDEN_SUNBEAMS, ForestWeatherType.resolveFromActivity(streak = 3, hourOfDay = 14))
        assertEquals(ForestWeatherType.GOLDEN_SUNBEAMS, ForestWeatherType.resolveFromActivity(streak = 7, hourOfDay = 11))

        // Early morning with normal streak triggers clear sunny daylight
        assertEquals(ForestWeatherType.CLEAR_SUNNY, ForestWeatherType.resolveFromActivity(streak = 1, hourOfDay = 8))

        // Low streak in afternoon triggers gentle rain
        assertEquals(ForestWeatherType.GENTLE_RAIN, ForestWeatherType.resolveFromActivity(streak = 0, hourOfDay = 15))
    }
}
