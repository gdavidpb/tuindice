package com.gdavidpb.tuindice.transactions.domain.repository

interface ResolutionRepository {
	suspend fun syncTransactions(uid: String)
}