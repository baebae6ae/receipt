package com.baebae.receipt.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * 발행된 주간 카드. 원자료와 고른 문구를 같이 저장해 언제 다시 열어도 같은 카드가 나온다.
 * 숨긴 항목·템플릿은 사용자가 결과 화면에서 바꾼 값.
 */
@Entity(tableName = "weekly_card")
data class WeeklyCardEntity(
    @PrimaryKey val weekStartEpochDay: Long,
    val statsJson: String,
    val phrasesJson: String,
    /** 줄바꿈으로 구분한 항목 키 */
    val hiddenItems: String,
    val template: String,
    val createdAt: Long,
    /** 결과 화면에서 처음 공개됐는지. 전면 광고는 첫 공개 때만 고려한다. */
    val revealed: Boolean,
)

@Dao
interface WeeklyCardDao {
    @Query("SELECT * FROM weekly_card ORDER BY weekStartEpochDay DESC")
    fun observeAll(): Flow<List<WeeklyCardEntity>>

    @Query("SELECT * FROM weekly_card WHERE weekStartEpochDay = :week")
    suspend fun get(week: Long): WeeklyCardEntity?

    @Query("SELECT * FROM weekly_card WHERE revealed = 0 ORDER BY weekStartEpochDay DESC LIMIT 1")
    suspend fun latestUnrevealed(): WeeklyCardEntity?

    @Upsert
    suspend fun upsert(entity: WeeklyCardEntity)

    @Query("UPDATE weekly_card SET hiddenItems = :hidden WHERE weekStartEpochDay = :week")
    suspend fun setHidden(week: Long, hidden: String)

    @Query("UPDATE weekly_card SET phrasesJson = :json WHERE weekStartEpochDay = :week")
    suspend fun setPhrases(week: Long, json: String)

    @Query("UPDATE weekly_card SET template = :template WHERE weekStartEpochDay = :week")
    suspend fun setTemplate(week: Long, template: String)

    @Query("UPDATE weekly_card SET revealed = 1 WHERE weekStartEpochDay = :week")
    suspend fun markRevealed(week: Long)
}
