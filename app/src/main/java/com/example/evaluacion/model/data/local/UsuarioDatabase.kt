package com.example.evaluacion.model.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UsuarioEntity::class, SolicitudEntity::class],
    version = 4
)
abstract class UsuarioDatabase : RoomDatabase() {
    abstract fun usuarioDao() : UsuarioDao

    abstract fun solicitudDao() : SolicitudDao

    companion object{
        @Volatile
        private var INSTANCE: UsuarioDatabase? = null

        // Migración de versión 1 a versión 2
        // Conserva los usuarios y crea la tabla solicitudes
        private val MIGRATION_1_2 = object : Migration(1, 2){
            override fun migrate(db: SupportSQLiteDatabase){
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `solicitudes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `titulo` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `categoria` TEXT NOT NULL,
                        `estado` TEXT NOT NULL,
                        `fechaCreacion` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        // Migración de la versión 2 a la versión 3
        // Agrega la columna de evidencia sin borrar registros
        private val MIGRATION_2_3 = object : Migration(2, 3){
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE solicitudes ADD COLUMN evidenciaPath TEXT"
                )
            }
        }

        // Migracion de la version 3 a la version 4
        // Agrega el rol y conserva los usuarios existentes
        private val MIGRATION_3_4 = object : Migration(3, 4) {

            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL(
                    """
                        ALTER TABLE usuarios
                         ADD COLUMN rol TEXT NOT NULL DEFAULT 'CLIENTE'
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): UsuarioDatabase{
            return INSTANCE ?: synchronized(this){
                Room.databaseBuilder(
                    context.applicationContext,
                    UsuarioDatabase::class.java,
                    "usuario_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}