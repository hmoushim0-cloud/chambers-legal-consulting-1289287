package sa.factory.chamberslegalconsu.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChambersLegalEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun legal_consultingDao(): ChambersLegalDao
}
