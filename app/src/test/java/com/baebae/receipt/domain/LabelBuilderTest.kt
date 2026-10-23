package com.baebae.receipt.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LabelBuilderTest {
    private val h = 3_600_000L
    private val monday = LocalDate.of(2026, 10, 19)

    @Test
    fun ingredientsAreSortedSharesOfMeasuredTime() {
        val stats = WeeklyStats(
            weekStart = monday, days = 7,
            apps = listOf(AppUsage("a", "A", 10 * h), AppUsage("b", "B", 5 * h), AppUsage("c", "C", 3 * h), AppUsage("d", "D", 2 * h), AppUsage("e", "E", h)),
            screenMs = 21 * h, unlocks = 0, steps = 60_000, sleepTotalMs = 49 * h, sleepNights = 7, lateNightMs = 0,
        )
        val ingredients = LabelBuilder.ingredients(stats, emptySet())
        // 화면 21h + 수면 49h + 걷기 60000보×0.6s = 10h → 80h
        assertEquals(listOf("ing:sleep", "ing:app:a", "ing:walk", "ing:app:b", "ing:app:c", "ing:app:d"), ingredients.map { it.key })
        assertEquals(49.0 / 80 * 100, ingredients[0].percent, 1e-9)
        assertTrue(ingredients.zipWithNext().all { (x, y) -> x.percent >= y.percent })
    }

    @Test
    fun hiddenAppsAreNotNamedButStayInTheDenominator() {
        val stats = WeeklyStats(monday, 7, listOf(AppUsage("a", "A", 10 * h), AppUsage("b", "B", 10 * h)), 20 * h, 0, 0, 0, 0, 0)
        val ingredients = LabelBuilder.ingredients(stats, setOf("a"))
        assertEquals(listOf("ing:app:b"), ingredients.map { it.key })
        assertEquals(50.0, ingredients.single().percent, 1e-9)
    }

    @Test
    fun quizAnswersProduceAWeek() {
        val stats = Quiz.toStats(listOf(3, 0, 2, 0, 2), monday) { it.name }
        assertEquals(7, stats.days)
        assertEquals("VIDEO", stats.apps.first().label)
        assertTrue(stats.lateNightMs.toDouble() / stats.screenMs > 0.2)
    }
}
