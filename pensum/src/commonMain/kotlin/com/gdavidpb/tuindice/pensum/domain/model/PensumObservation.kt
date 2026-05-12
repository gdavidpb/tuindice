package com.gdavidpb.tuindice.pensum.domain.model

sealed interface PensumObservation {
	data object WaitingForRecordData : PensumObservation

	data object RecordDataUnavailable : PensumObservation

	data object Missing : PensumObservation

	data class Content(
		val pensum: ObservedPensum
	) : PensumObservation
}
