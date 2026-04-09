package com.gdavidpb.tuindice.persistence.domain.repository

interface PersistenceTransactionRunner {
	suspend fun <R> immediate(block: suspend () -> R): R
}
