package com.gdavidpb.tuindice.pensum.data.source

import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumRepository
import com.gdavidpb.tuindice.persistence.domain.model.PensumCreditsProgress
import com.gdavidpb.tuindice.persistence.domain.repository.PensumProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

// A map over the flow the pensum screen already observes: the same two numbers the screen shows, and
// no download of its own. It emits only when those numbers (or their absence) change.
class PensumProgressDataSource(
	private val pensumRepository: PensumRepository
) : PensumProgressRepository {
	override fun observePensumProgressFlow(): Flow<PensumCreditsProgress?> {
		return pensumRepository.observePensumFlow().map { observation ->
			when (observation) {
				is PensumObservation.Content -> PensumCreditsProgress(
					approvedCredits = observation.pensum.approvedCredits,
					totalCredits = observation.pensum.pensum.totalCredits
				)

				PensumObservation.Missing,
				PensumObservation.WaitingForRecordData,
				PensumObservation.RecordDataUnavailable -> null
			}
		}.distinctUntilChanged()
	}
}
