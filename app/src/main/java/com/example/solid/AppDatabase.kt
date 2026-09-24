package com.example.solid
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Session::class, Workout::class, WorkoutSet::class, BodyWeightEntry::class, WorkoutNote::class, NoteEntry::class, DashboardCard::class],
    version = 6
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sessions ADD COLUMN color INTEGER NOT NULL DEFAULT 0")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE sessions ADD COLUMN timestamp INTEGER NOT NULL DEFAULT 0")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS body_weight_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        weight REAL NOT NULL,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE workout_sets ADD COLUMN isPartial INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE workout_sets ADD COLUMN difficulty TEXT DEFAULT NULL")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS workout_notes (
                        exerciseName TEXT PRIMARY KEY NOT NULL,
                        note TEXT NOT NULL
                    )
                """)
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS note_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        typeName TEXT NOT NULL,
                        note TEXT NOT NULL,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                """)
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS dashboard_cards (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cardType TEXT NOT NULL,
                        scopeType TEXT NOT NULL,
                        scopeValue TEXT,
                        metric TEXT NOT NULL DEFAULT 'WEIGHT',
                        `order` INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}