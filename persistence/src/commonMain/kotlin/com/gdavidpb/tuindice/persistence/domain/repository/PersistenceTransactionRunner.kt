package com.gdavidpb.tuindice.persistence.domain.repository

interface PersistenceTransactionRunner {
	suspend fun <R> immediate(block: suspend () -> R): R

	/**
	 * Runs [block] inside one read transaction: every read it makes sees the same committed state,
	 * so two tables written together by one transaction are never read half old, half new. For
	 * reads only; writes belong to [immediate].
	 */
	suspend fun <R> read(block: suspend () -> R): R
}
