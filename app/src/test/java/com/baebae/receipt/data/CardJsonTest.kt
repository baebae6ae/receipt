package com.baebae.receipt.data

import com.baebae.receipt.domain.AppUsage
import com.baebae.receipt.domain.ChosenPhrases
import com.baebae.receipt.domain.Phrase
import com.baebae.receipt.domain.WeeklyStats
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CardJsonTest {

    @Test
    fun statsRoundTrip() {
        val stats = WeeklyStats(
            weekStart = LocalDate.of(2026, 10, 19),
            days = 7,
            apps = listOf(AppUsage("com.a", "에이 \"앱\"", 1_000), AppUsage("com.b", "B", 500)),
            screenMs = 1_500,
            unlocks = 42,
            steps = 12_345,
            sleepTotalMs = 9_000,
            sleepNights = 3,
            lateNightMs = 100,
        )
        assertEquals(stats, CardJson.statsFromJson(CardJson.statsToJson(stats)))
    }

    @Test
    fun phrasesRoundTripWithNulls() {
        val phrases = ChosenPhrases(listOf(Phrase("caution.a.1", "문구")), storage = null, expiry = Phrase("expiry.b.2", "x"))
        assertEquals(phrases, CardJson.phrasesFromJson(CardJson.phrasesToJson(phrases)))
    }

    @Test
    fun hiddenRoundTrip() {
        val hidden = setOf("ing:app:com.a", "nut:steps")
        assertEquals(hidden, CardJson.hiddenFromString(CardJson.hiddenToString(hidden)))
        assertEquals(emptySet<String>(), CardJson.hiddenFromString(""))
    }
}
