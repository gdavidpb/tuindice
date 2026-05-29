package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumCacheTable
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumSelectionTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectCatalogCacheTable

object TuIndiceRoomMigrations {
	val all: Array<Migration>
		get() = arrayOf(Migration30To31)
}

private object Migration30To31 : Migration(30, 31) {
	override fun migrate(connection: SQLiteConnection) {
		connection.clearPensumDerivedTables()
	}
}

private fun SQLiteConnection.clearPensumDerivedTables() {
	execSQL("DELETE FROM ${PensumSelectionTable.TABLE_NAME}")
	execSQL("DELETE FROM ${PensumCacheTable.TABLE_NAME}")
	execSQL("DELETE FROM ${SubjectCatalogCacheTable.TABLE_NAME}")
}
