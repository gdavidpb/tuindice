package com.gdavidpb.tuindice.transactions.data.repository.transactions.source.database.resolution

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution

interface ResolutionHandler {
	suspend fun match(resolution: Resolution): Boolean
	suspend fun apply(resolution: Resolution)
}