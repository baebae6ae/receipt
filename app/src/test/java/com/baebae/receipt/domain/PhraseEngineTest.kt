package com.baebae.receipt.domain

import com.baebae.receipt.domain.phrase.PhraseBank
import com.baebae.receipt.domain.phrase.PhraseCategory
import com.baebae.receipt.domain.phrase.PhraseMetrics
import com.baebae.receipt.domain.phrase.PhraseSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import kotlin.random.Random

class PhraseEngineTest {

    private fun load(lang: String): PhraseBank {
        // Gradle 단위 테스트의 작업 디렉터리는 모듈(app/). 다른 곳에서 돌릴 때는 -Dphrases.dir 로 지정한다.
        val dir = System.getProperty("phrases.dir") ?: "src/main/assets"
        val file = File(dir, "phrases_$lang.json")
        return PhraseBank.parse(file.readText())
    }

    private val ko = load("ko")
    private val en = load("en")
    private val h = 3_600_000L
    private val monday = LocalDate.of(2026, 10, 19)

    private fun stats(
        screenHoursPerDay: Double = 3.0,
        lateNightRatio: Double = 0.05,
        sleepHours: Double? = 7.0,
        stepsPerDay: Long = 6000,
        unlocksPerDay: Int = 50,
        topShare: Double = 0.3,
    ): WeeklyStats {
        val screen = (screenHoursPerDay * 7 * h).toLong()
        return WeeklyStats(
            weekStart = monday, days = 7,
            apps = listOf(AppUsage("com.top", "TopApp", (screen * topShare).toLong()), AppUsage("com.b", "B", (screen * 0.1).toLong())),
            screenMs = screen, unlocks = unlocksPerDay * 7, steps = stepsPerDay * 7,
            sleepTotalMs = sleepHours?.let { (it * 7 * h).toLong() } ?: 0, sleepNights = if (sleepHours != null) 7 else 0,
            lateNightMs = (screen * lateNightRatio).toLong(),
        )
    }

    @Test
    fun everyGroupHasAtLeastTenPhrasesAndLanguagesMatch() {
        for (bank in listOf(ko, en)) {
            for ((name, def) in bank.categories) {
                assertTrue(name, def.groups.any { it.isDefault })
                def.groups.forEach { assertTrue("$name.${it.id}", it.phrases.size >= 10) }
            }
        }
        assertEquals(ko.categories.keys, en.categories.keys)
        for (name in ko.categories.keys) {
            assertEquals(
                ko.categories.getValue(name).groups.map { it.id to it.phrases.size },
                en.categories.getValue(name).groups.map { it.id to it.phrases.size },
            )
        }
    }

    @Test
    fun noPersonalityTypeLabels() {
        val banned = listOf("당신은", "형입니다", "유형", "You are", "type of person")
        for (bank in listOf(ko, en)) bank.categories.values.flatMap { it.groups }.flatMap { it.phrases }.forEach { phrase ->
            banned.forEach { assertFalse("\"$phrase\" contains \"$it\"", phrase.contains(it)) }
        }
    }

    @Test
    fun lateNightUsagePicksLateNightCaution() {
        val metrics = PhraseMetrics.from(stats(lateNightRatio = 0.3))
        repeat(20) { seed ->
            val picked = PhraseSelector.select(ko, PhraseCategory.CAUTION, metrics, emptySet(), Random(seed))
            assertEquals(2, picked.size)
            assertTrue(picked.first().id.startsWith("caution.late_night."))
        }
    }

    @Test
    fun noDataFallsBackToDefaultsOnly() {
        val empty = WeeklyStats.empty(monday)
        val metrics = PhraseMetrics.from(empty)
        val picked = PhraseSelector.select(ko, PhraseCategory.CAUTION, metrics, emptySet(), Random(1))
        assertEquals(2, picked.size)
        assertTrue(picked.all { it.id.startsWith("caution.default.") })
        assertNotEquals(picked[0].id, picked[1].id)
    }

    @Test
    fun avoidsRecentPhrasesWhileOthersRemain() {
        val metrics = PhraseMetrics.from(stats())
        val recent = (0 until 9).map { "storage.default.$it" }.toSet()
        repeat(10) { seed ->
            val picked = PhraseSelector.select(ko, PhraseCategory.STORAGE, metrics, recent, Random(seed)).single()
            assertEquals("storage.default.9", picked.id)
        }
    }

    @Test
    fun excludeNeverPicksCurrent() {
        val metrics = PhraseMetrics.from(stats(sleepHours = 5.0))
        val first = PhraseSelector.select(ko, PhraseCategory.EXPIRY, metrics, emptySet(), Random(3)).single()
        repeat(30) { seed ->
            val next = PhraseSelector.select(ko, PhraseCategory.EXPIRY, metrics, emptySet(), Random(seed), exclude = setOf(first.id)).single()
            assertNotEquals(first.id, next.id)
        }
    }

    @Test
    fun idsResolveAcrossLanguages() {
        val id = PhraseSelector.select(ko, PhraseCategory.CAUTION, PhraseMetrics.from(stats()), emptySet(), Random(5)).first().id
        assertTrue(en.text(id)!!.isNotBlank())
    }

    @Test
    fun missingStepsDoNotTriggerLowStepPhrases() {
        val metrics = PhraseMetrics.from(stats(stepsPerDay = 0))
        assertTrue(metrics.getValue(PhraseMetrics.DAILY_STEPS).isNaN())
    }

    @Test
    fun topAppPlaceholderRespectsHiddenApps() {
        val s = stats()
        val phrases = ChosenPhrases(listOf(Phrase("x", "{topApp} 함유")), null, null)
        assertEquals("TopApp 함유", LabelBuilder.build(s, "나", phrases).cautions.single().text)
        assertEquals("B 함유", LabelBuilder.build(s, "나", phrases, hiddenPackages = setOf("com.top")).cautions.single().text)
        assertEquals("휴대폰 함유", LabelBuilder.build(s, "나", phrases, hiddenPackages = setOf("com.top", "com.b"), topAppFallback = "휴대폰").cautions.single().text)
    }
}
