package com.gdavidpb.tuindice.transactions.data.room.resolution

import androidx.room.withTransaction
import com.gdavidpb.tuindice.base.domain.model.resolution.Resolution
import com.gdavidpb.tuindice.persistence.data.room.TuIndiceDatabase

class ResolutionApplier(
	private val room: TuIndiceDatabase,
	private val resolutionHandlers: List<ResolutionHandler>
) {
	suspend fun apply(resolutions: List<Resolution>) {
		room.withTransaction {
			resolutions.forEach { resolution ->
				val handler = resolutionHandlers.find { handler ->
					handler.match(
						resolution
					)
				}

				handler?.apply(resolution)
			}
		}
	}
}