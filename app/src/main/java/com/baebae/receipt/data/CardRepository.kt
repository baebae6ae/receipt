package com.baebae.receipt.data

import android.content.Context
import com.baebae.receipt.R
import com.baebae.receipt.data.db.WeeklyCardDao
import com.baebae.receipt.data.db.WeeklyCardEntity
import com.baebae.receipt.data.steps.StepRepository
import com.baebae.receipt.domain.ChosenPhrases
import com.baebae.receipt.domain.LabelBuilder
import com.baebae.receipt.domain.LabelCard
import com.baebae.receipt.domain.PhrasePicker
import com.baebae.receipt.domain.WeeklyStats
import com.baebae.receipt.util.WeekMath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZonedDateTime

/** 카드 한 장의 저장 상태. [published]가 false 면 진행 중인 주의 미리보기(저장 안 됨). */
data class StoredCard(
    val weekStart: LocalDate,
    val stats: WeeklyStats,
    val phrases: ChosenPhrases,
    val hidden: Set<String>,
    val template: String,
    val revealed: Boolean,
    val published: Boolean,
)

class CardRepository(
    private val context: Context,
    private val prefs: AppPrefs,
    private val stats: StatsRepository,
    private val steps: StepRepository,
    private val dao: WeeklyCardDao,
    private val phrases: PhrasePicker,
) {

    fun productName(): String = prefs.nickname.ifEmpty { context.getString(R.string.default_nickname) }

    /** 저장 상태 → 화면에 그릴 카드. 숨긴 앱 설정은 지난 카드에도 그때그때 적용한다. */
    fun toLabel(card: StoredCard): LabelCard = LabelBuilder.build(
        stats = card.stats,
        productName = productName(),
        phrases = phrases.localize(card.phrases),
        hiddenPackages = prefs.hiddenPackages,
        hiddenItems = card.hidden,
        topAppFallback = phrases.topAppFallback(),
    )

    /** [category] 문구를 다시 뽑는다. 발행된 카드면 저장하고 사용 기록을 남긴다. */
    suspend fun reroll(card: StoredCard, category: String): StoredCard {
        val next = card.copy(phrases = phrases.reroll(card.stats, card.phrases, category, record = card.published))
        if (card.published) setPhrases(card.weekStart, next.phrases)
        return next
    }

    /** 진행 중인 이번 주. 권한이 없으면 null. */
    suspend fun liveCurrentWeek(): StoredCard? {
        // 앱이 열려 있을 때는 센서 값을 확실히 받을 수 있으니 이때도 스냅샷을 남긴다.
        steps.snapshotIfStale()
        val weekStart = WeekMath.currentWeekStart()
        val weekStats = stats.weekStats(weekStart) ?: return null
        return StoredCard(weekStart, weekStats, phrases.preview(weekStats), emptySet(), DEFAULT_TEMPLATE, revealed = true, published = false)
    }

    suspend fun get(weekStart: LocalDate): StoredCard? = dao.get(weekStart.toEpochDay())?.toStored()

    /** 발행됐지만 아직 열어 보지 않은 가장 최근 카드. */
    suspend fun latestUnrevealed(): StoredCard? = dao.latestUnrevealed()?.toStored()

    fun observeArchive(): Flow<List<StoredCard>> = dao.observeAll().map { list -> list.map { it.toStored() } }

    /**
     * 그 주 카드를 확정해 저장한다. 이미 있으면 그대로 돌려준다.
     * 권한이 없거나 그 주에 기록이 전혀 없으면 null.
     */
    suspend fun publish(weekStart: LocalDate, now: ZonedDateTime = ZonedDateTime.now()): StoredCard? {
        get(weekStart)?.let { return it }
        steps.snapshotIfStale()
        val weekStats = stats.weekStats(weekStart, now) ?: return null
        if (weekStats.screenMs == 0L && weekStats.steps == 0L && weekStats.sleepNights == 0) return null
        val entity = WeeklyCardEntity(
            weekStartEpochDay = weekStart.toEpochDay(),
            statsJson = CardJson.statsToJson(weekStats),
            phrasesJson = CardJson.phrasesToJson(phrases.pickForPublish(weekStats)),
            hiddenItems = "",
            template = DEFAULT_TEMPLATE,
            createdAt = now.toInstant().toEpochMilli(),
            revealed = false,
        )
        dao.upsert(entity)
        return entity.toStored()
    }

    suspend fun setHidden(weekStart: LocalDate, hidden: Set<String>) =
        dao.setHidden(weekStart.toEpochDay(), CardJson.hiddenToString(hidden))

    suspend fun setPhrases(weekStart: LocalDate, chosen: ChosenPhrases) =
        dao.setPhrases(weekStart.toEpochDay(), CardJson.phrasesToJson(chosen))

    suspend fun setTemplate(weekStart: LocalDate, template: String) =
        dao.setTemplate(weekStart.toEpochDay(), template)

    suspend fun markRevealed(weekStart: LocalDate) = dao.markRevealed(weekStart.toEpochDay())

    private fun WeeklyCardEntity.toStored() = StoredCard(
        weekStart = LocalDate.ofEpochDay(weekStartEpochDay),
        stats = CardJson.statsFromJson(statsJson),
        phrases = CardJson.phrasesFromJson(phrasesJson),
        hidden = CardJson.hiddenFromString(hiddenItems),
        template = template,
        revealed = revealed,
        published = true,
    )

    companion object {
        const val DEFAULT_TEMPLATE = "CLASSIC"
    }
}
