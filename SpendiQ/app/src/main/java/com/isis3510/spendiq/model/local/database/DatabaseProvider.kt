package com.isis3510.spendiq.model.local.database

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    // Definición de la migración de la versión 1 a la 2
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Aquí implementas los cambios necesarios para migrar la base de datos
            // Por ejemplo, crear la nueva tabla 'limits'
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `limits` (
                    `userId` TEXT NOT NULL,
                    `frequency` TEXT NOT NULL,
                    `isByExpenseChecked` INTEGER NOT NULL,
                    `isByQuantityChecked` INTEGER NOT NULL,
                    `expenses` TEXT NOT NULL,
                    `totalAmount` TEXT NOT NULL,
                    PRIMARY KEY(`userId`)
                )
            """.trimIndent())
        }
    }

    fun getDatabase(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "offers_database"
                    )
                        .addMigrations(MIGRATION_1_2)
                        .build()
                }
            }
        }
        return INSTANCE!!
    }
}
