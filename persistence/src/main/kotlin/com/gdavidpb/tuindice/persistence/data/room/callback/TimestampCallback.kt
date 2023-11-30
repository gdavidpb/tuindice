package com.gdavidpb.tuindice.persistence.data.room.callback

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gdavidpb.tuindice.persistence.data.room.schema.BaseTable
import com.gdavidpb.tuindice.persistence.data.room.schema.EvaluationTable

class TimestampCallback : RoomDatabase.Callback() {
	override fun onCreate(db: SupportSQLiteDatabase) {
		super.onCreate(db)

		val entities = listOf(
			EvaluationTable.TABLE_NAME
		)

		entities.forEach { entity ->
			db.execSQL(
				"""
					CREATE TRIGGER IF NOT EXISTS on_insert_${entity}_trigger
					AFTER INSERT ON $entity
					BEGIN
					    UPDATE $entity 
					    SET ${BaseTable.MODIFIED_AT} = (strftime('%s','now') * 1000)
					    WHERE rowid = NEW.rowid;
					END;
			        """
			)

			db.execSQL(
				"""
					CREATE TRIGGER IF NOT EXISTS on_update_${entity}_trigger
					AFTER UPDATE ON $entity
					WHEN OLD.${BaseTable.MODIFIED_AT} < (strftime('%s','now') * 1000)
					BEGIN
					    UPDATE $entity
					    SET ${BaseTable.MODIFIED_AT} = (strftime('%s','now') * 1000)
					    WHERE rowid = NEW.rowid;
					END;

                    """
			)
		}
	}
}