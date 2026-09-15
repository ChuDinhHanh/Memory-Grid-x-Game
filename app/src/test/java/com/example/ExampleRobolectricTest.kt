package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GameMode
import com.example.engine.DifficultyEngine
import com.example.engine.PatternGenerator
import com.example.engine.ScoringEngine
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
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Memory Grid", appName)
    }

    @Test
    fun `difficulty engine scales correctly`() {
        val lvl1 = DifficultyEngine.calculateDifficulty(1, GameMode.CLASSIC)
        assertEquals(3, lvl1.gridSize)
        assertEquals(3, lvl1.cellsToRemember)

        val lvl5 = DifficultyEngine.calculateDifficulty(5, GameMode.CLASSIC)
        assertEquals(4, lvl5.gridSize)
        assertEquals(6, lvl5.cellsToRemember)
    }

    @Test
    fun `pattern generator selects unique cells`() {
        val pattern = PatternGenerator.generatePattern(gridSize = 4, count = 5)
        assertEquals(5, pattern.size)
        assertEquals(5, pattern.toSet().size)
        assertTrue(pattern.all { it in 0 until 16 })
    }

    @Test
    fun `scoring engine calculates streak multipliers`() {
        assertEquals(1.0f, ScoringEngine.getStreakMultiplier(1))
        assertEquals(1.2f, ScoringEngine.getStreakMultiplier(3))
        assertEquals(1.5f, ScoringEngine.getStreakMultiplier(6))
        assertEquals(2.0f, ScoringEngine.getStreakMultiplier(10))
    }
}
