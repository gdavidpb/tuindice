package com.gdavidpb.tuindice.persistence.data.room

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumCacheTable
import com.gdavidpb.tuindice.persistence.data.room.schema.PensumSelectionTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SubjectCatalogCacheTable
import com.gdavidpb.tuindice.persistence.data.room.schema.SyntheticTermLoadPreviewCacheTable

object TuIndiceRoomMigrations {
	val all: Array<Migration>
		get() = arrayOf(Migration30To31, Migration31To32)
}

private object Migration30To31 : Migration(30, 31) {
	override fun migrate(connection: SQLiteConnection) {
		connection.clearPensumDerivedTables()
	}
}

private object Migration31To32 : Migration(31, 32) {
	override fun migrate(connection: SQLiteConnection) {
		connection.clearSyntheticTermLoadPreviewCache()
		connection.addSyntheticTermLoadPreviewConfidenceColumns()
	}
}

private fun SQLiteConnection.clearPensumDerivedTables() {
	execSQL("DELETE FROM ${PensumSelectionTable.TABLE_NAME}")
	execSQL("DELETE FROM ${PensumCacheTable.TABLE_NAME}")
	execSQL("DELETE FROM ${SubjectCatalogCacheTable.TABLE_NAME}")
}

private fun SQLiteConnection.clearSyntheticTermLoadPreviewCache() {
	execSQL("DELETE FROM ${SyntheticTermLoadPreviewCacheTable.TABLE_NAME}")
}

private fun SQLiteConnection.addSyntheticTermLoadPreviewConfidenceColumns() {
	execSQL("ALTER TABLE ${SyntheticTermLoadPreviewCacheTable.TABLE_NAME} ADD COLUMN ${SyntheticTermLoadPreviewCacheTable.BASIS} TEXT")
	execSQL("ALTER TABLE ${SyntheticTermLoadPreviewCacheTable.TABLE_NAME} ADD COLUMN ${SyntheticTermLoadPreviewCacheTable.CONFIDENCE} TEXT")
	execSQL("ALTER TABLE ${SyntheticTermLoadPreviewCacheTable.TABLE_NAME} ADD COLUMN ${SyntheticTermLoadPreviewCacheTable.DETAIL} TEXT")
}
