package com.gdavidpb.tuindice.data.source.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gdavidpb.tuindice.data.model.database.AccountEntity
import com.gdavidpb.tuindice.data.model.database.QuarterEntity
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.data.source.database.dao.DstAccountDao
import com.gdavidpb.tuindice.data.source.database.dao.DstQuarterDao
import com.gdavidpb.tuindice.data.source.database.dao.DstSubjectDao
import com.gdavidpb.tuindice.data.source.database.dao.converter.DatabaseConverters
import com.gdavidpb.tuindice.data.utils.DATABASE_VERSION

@Database(entities = [
    AccountEntity::class,
    QuarterEntity::class,
    SubjectEntity::class],
        version = DATABASE_VERSION,
        exportSchema = false)
@TypeConverters(DatabaseConverters::class)
abstract class SQLiteDatabase : RoomDatabase() {
    abstract val accounts: DstAccountDao
    abstract val quarters: DstQuarterDao
    abstract val subjects: DstSubjectDao
}