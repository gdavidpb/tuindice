package com.gdavidpb.tuindice.transactions.domain.model

interface ResolutionHandler {
	suspend fun apply(resolution: Resolution)
}