package io.github.ducthinh0304.javainterviewprep.core.engine.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        QuestionStatEntity::class,
        NoteProgressEntity::class,
        DailyAnswerEntity::class,
        DailyActivityEntity::class,
        UserStatsEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EngineDatabase : RoomDatabase() {
    abstract fun questionStatDao(): QuestionStatDao
    abstract fun noteProgressDao(): NoteProgressDao
    abstract fun dailyAnswerDao(): DailyAnswerDao
    abstract fun dailyActivityDao(): DailyActivityDao
    abstract fun userStatsDao(): UserStatsDao
}
