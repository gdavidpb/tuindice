package com.gdavidpb.tuindice.persistence.domain.repository

interface PersistenceTransactionRunner {
	suspend fun <R> immediate(block: suspend () -> R): R

	/**
	 * Runs [block] inside one read transaction: every read it makes sees the same committed state,
	 * so two tables written together by one transaction are never read half old, half new. For
	 * reads only; writes belong to [immediate]. [block] may call the DAOs and nothing that suspends on
	 * anything else: the reader connection is held for as long as it runs.
	 *
	 * What the guarantee rests on: a reader connection that is not the writer's, in WAL mode, so the
	 * deferred transaction pins a snapshot while a write commits. iOS opens the file with the bundled
	 * SQLite driver (createIosDatabase), where RoomPersistenceTransactionRunnerIsolationTest proves it
	 * with two connections, and that is the only place the isolation, and that a read does not hold up the
	 * writer, is demonstrated. Android opens it through the framework helper (createAndroidDatabase sets
	 * no driver, so Room serves every connection from one passthrough connection): `read` has no
	 * execution at all on Android in this repository's tests (the host has no driver) and is exercised
	 * for the first time by the native E2E. A reader must therefore not depend on it alone: it validates the pair it read
	 * (see RoomDatabaseDataSource.readDisplayContext).
	 */
	suspend fun <R> read(block: suspend () -> R): R
}
