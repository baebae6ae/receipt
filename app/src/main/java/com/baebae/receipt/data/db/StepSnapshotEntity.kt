package com.baebae.receipt.data.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

/** 걸음 센서 누적값 스냅샷. [bootMillis]가 바뀌면 재부팅으로 누적값이 0부터 다시 센 것이다. */
@Entity(tableName = "step_snapshot", indices = [Index("timeMillis")])
data class StepSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timeMillis: Long,
    val counter: Long,
    val bootMillis: Long,
)

@Dao
interface StepSnapshotDao {
    @Insert
    suspend fun insert(snapshot: StepSnapshotEntity)

    @Query("SELECT * FROM step_snapshot WHERE timeMillis BETWEEN :from AND :to ORDER BY timeMillis")
    suspend fun between(from: Long, to: Long): List<StepSnapshotEntity>

    @Query("SELECT * FROM step_snapshot ORDER BY timeMillis DESC LIMIT 1")
    suspend fun latest(): StepSnapshotEntity?

    @Query("DELETE FROM step_snapshot WHERE timeMillis < :before")
    suspend fun deleteOlderThan(before: Long)
}
