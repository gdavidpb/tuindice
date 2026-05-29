package com.gdavidpb.tuindice.persistence.data.room

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteStatement
import kotlin.test.Test
import kotlin.test.assertEquals

class TuIndiceRoomMigrationsTest {
	@Test
	fun migrationsClearPensumDerivedTables() {
		val migrations = TuIndiceRoomMigrations.all

		assertEquals(
			listOf(30 to 31),
			migrations.map { migration -> migration.startVersion to migration.endVersion }
		)

		migrations.forEach { migration ->
			val connection = CapturingSQLiteConnection()

			migration.migrate(connection)

			assertEquals(
				listOf(
					"DELETE FROM pensum_selection",
					"DELETE FROM pensum_cache",
					"DELETE FROM subject_catalog_cache"
				),
				connection.executedSql
			)
		}
	}
}

private class CapturingSQLiteConnection : SQLiteConnection {
	val executedSql = mutableListOf<String>()

	override fun prepare(sql: String): SQLiteStatement {
		executedSql += sql
		return NoOpSQLiteStatement
	}

	override fun close() = Unit
}

private object NoOpSQLiteStatement : SQLiteStatement {
	override fun bindBlob(index: Int, value: ByteArray) = unexpected()
	override fun bindDouble(index: Int, value: Double) = unexpected()
	override fun bindLong(index: Int, value: Long) = unexpected()
	override fun bindText(index: Int, value: String) = unexpected()
	override fun bindNull(index: Int) = unexpected()
	override fun getBlob(index: Int): ByteArray = unexpected()
	override fun getDouble(index: Int): Double = unexpected()
	override fun getLong(index: Int): Long = unexpected()
	override fun getText(index: Int): String = unexpected()
	override fun isNull(index: Int): Boolean = unexpected()
	override fun getColumnCount(): Int = unexpected()
	override fun getColumnName(index: Int): String = unexpected()
	override fun getColumnType(index: Int): Int = unexpected()
	override fun step(): Boolean = false
	override fun reset() = unexpected()
	override fun clearBindings() = unexpected()
	override fun close() = Unit

	private fun unexpected(): Nothing {
		error("Unexpected SQLiteStatement operation.")
	}
}
