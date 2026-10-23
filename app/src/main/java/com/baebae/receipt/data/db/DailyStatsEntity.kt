package com.baebae.receipt.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/** 하루치 사용 요약 캐시. 시스템이 오래된 UsageEvents 를 지워도 주간 집계가 유지되도록 한다. */
@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val epochDay: Long,
    /** [{"p": 패키지, "l": 앱 이름, "ms": 전경 시간}] */
    val appsJson: String,
    val unlocks: Int,
    val lateNightMs: Long,
    val sleepMs: Long?,
    val updatedAt: Long,
)

@Dao
interface DailyStatsDao {
    @Upsert
    suspend fun upsert(entity: DailyStatsEntity)

    @Query("SELECT * FROM daily_stats WHERE epochDay BETWEEN :from AND :to")
    suspend fun between(from: Long, to: Long): List<DailyStatsEntity>

    @Query("DELETE FROM daily_stats WHERE epochDay < :before")
    suspend fun deleteOlderThan(before: Long)
}
