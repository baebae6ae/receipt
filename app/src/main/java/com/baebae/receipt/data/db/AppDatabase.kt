package com.baebae.receipt.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [StepSnapshotEntity::class, DailyStatsEntity::class, WeeklyCardEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stepSnapshots(): StepSnapshotDao
    abstract fun dailyStats(): DailyStatsDao
    abstract fun weeklyCards(): WeeklyCardDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "receipt.db").build()
    }
}
