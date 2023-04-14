package com.gdavidpb.tuindice.transactions.data.room.resolution

import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution

interface ResolutionHandler {
	suspend fun match(resolution: Resolution): Boolean
	suspend fun apply(resolution: Resolution)
}