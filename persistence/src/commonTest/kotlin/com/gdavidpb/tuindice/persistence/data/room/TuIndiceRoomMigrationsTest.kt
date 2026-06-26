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
			listOf(30 to 31, 31 to 32, 32 to 33),
			migrations.map { migration -> migration.startVersion to migration.endVersion }
		)

		val executedSqlByMigration = migrations.associate { migration ->
			val connection = CapturingSQLiteConnection()

			migration.migrate(connection)
			(migration.startVersion to migration.endVersion) to connection.executedSql
		}
		val migration30To31Sql = executedSqlByMigration[30 to 31].orEmpty()
		val migration31To32Sql = executedSqlByMigration[31 to 32].orEmpty()
		val migration32To33Sql = executedSqlByMigration[32 to 33].orEmpty()

		assertEquals(
			listOf(
				"DELETE FROM pensum_selection",
				"DELETE FROM pensum_cache",
				"DELETE FROM subject_catalog_cache"
			),
			migration30To31Sql
		)
		assertEquals(
			listOf(
				"DELETE FROM synthetic_term_load_preview_cache",
				"ALTER TABLE synthetic_term_load_preview_cache ADD COLUMN basis TEXT",
				"ALTER TABLE synthetic_term_load_preview_cache ADD COLUMN confidence TEXT",
				"ALTER TABLE synthetic_term_load_preview_cache ADD COLUMN detail TEXT"
			),
			migration31To32Sql
		)
		assertEquals(
			listOf("ALTER TABLE pensum_selection ADD COLUMN inferred INTEGER NOT NULL DEFAULT 1"),
			migration32To33Sql
		)
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
