package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.ObservedRecord
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.filteredProjectionFor
import com.gdavidpb.tuindice.record.domain.model.other
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.repository.RecordSelectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveRecordUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	private val recordSelectionRepository: RecordSelectionRepository,
	override val reportingRepository: ReportingRepository,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<Unit, ObservedRecord, Nothing>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
	override suspend fun executeOnBackground(params: Unit): Flow<ObservedRecord> {
		return combine(
			academicRecordRepository.observeAcademicRecordSnapshotFlow(),
			recordSelectionRepository.observeRecordViewMode(),
			recordSelectionRepository.observeSelectedTermId(RecordViewMode.Historical),
			recordSelectionRepository.observeSelectedTermId(RecordViewMode.Projection)
		) { recordSnapshot, viewMode, selectedHistoricalTermId, selectedProjectionTermId ->
			val record = recordSnapshot.value
			val visibleTermIds = record.filteredProjectionFor(viewMode)
				.terms
				.map { term -> term.id }

			val selectedTermId = resolveSelectedTermId(
				viewMode = viewMode,
				visibleTermIds = visibleTermIds,
				selectedHistoricalTermId = selectedHistoricalTermId,
				selectedProjectionTermId = selectedProjectionTermId
			)

			if ((selectedTermId != null) && (selectedTermId != currentSelectedTermId(
					viewMode = viewMode,
					selectedHistoricalTermId = selectedHistoricalTermId,
					selectedProjectionTermId = selectedProjectionTermId
				))
			) {
				recordSelectionRepository.setSelectedTermId(
					viewMode = viewMode,
					termId = selectedTermId
				)
			}

			ObservedRecord(
				record = record,
				viewMode = viewMode,
				selectedTermId = selectedTermId,
				hasSyncedRecord = recordSnapshot.hasSynced
			)
		}
	}

	private fun resolveSelectedTermId(
		viewMode: RecordViewMode,
		visibleTermIds: List<String>,
		selectedHistoricalTermId: String?,
		selectedProjectionTermId: String?
	): String? {
		if (visibleTermIds.isEmpty()) return null

		val currentSelected = currentSelectedTermId(
			viewMode = viewMode,
			selectedHistoricalTermId = selectedHistoricalTermId,
			selectedProjectionTermId = selectedProjectionTermId
		)
		val mirroredSelected = currentSelectedTermId(
			viewMode = viewMode.other(),
			selectedHistoricalTermId = selectedHistoricalTermId,
			selectedProjectionTermId = selectedProjectionTermId
		)

		return listOfNotNull(currentSelected, mirroredSelected)
			.firstOrNull { candidate -> candidate in visibleTermIds }
			?: visibleTermIds.first()
	}

	private fun currentSelectedTermId(
		viewMode: RecordViewMode,
		selectedHistoricalTermId: String?,
		selectedProjectionTermId: String?
	): String? {
		return when (viewMode) {
			RecordViewMode.Historical -> selectedHistoricalTermId
			RecordViewMode.Projection -> selectedProjectionTermId
		}
	}
}
