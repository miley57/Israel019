package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MaterialTransaction::class,
        StructuralUpdate::class,
        EquipmentLog::class,
        SecurityLog::class,
        IncidentLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CatisDatabase : RoomDatabase() {
    abstract fun catisDao(): CatisDao

    companion object {
        @Volatile
        private var INSTANCE: CatisDatabase? = null

        fun getDatabase(context: Context): CatisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CatisDatabase::class.java,
                    "catis_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
