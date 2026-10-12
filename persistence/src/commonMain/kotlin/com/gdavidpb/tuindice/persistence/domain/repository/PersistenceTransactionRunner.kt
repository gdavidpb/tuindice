package com.gdavidpb.tuindice.persistence.domain.repository

interface PersistenceTransactionRunner {
	suspend fun <R> immediate(block: suspend () -> R): R

	/**
	 * Runs [block] inside one read transaction: every read it makes sees the same committed state,
	 * so two tables written together by one transaction are never read half old, half new. For
	 * reads only; writes belong to [immediate].
	 *
	 * What the guarantee rests on: a reader connection that is not the writer's, in WAL mode, so the
	 * deferred transaction pins a snapshot while a write commits. iOS opens the file with the bundled
	 * SQLite driver (createIosDatabase), where RoomPersistenceTransactionRunnerIsolationTest proves it
	 * with two connections. Android opens it through the framework helper (createAndroidDatabase sets no
	 * driver, so Room serves every connection from one passthrough connection), and no test of this
	 * repository can show the isolation there: the host has no driver and instrumented tests are out of
	 * scope. A reader must therefore not depend on it alone: it validates the pair it read
	 * (see RoomDatabaseDataSource.readDisplayContext).
	 */
	suspend fun <R> read(block: suspend () -> R): R
}
