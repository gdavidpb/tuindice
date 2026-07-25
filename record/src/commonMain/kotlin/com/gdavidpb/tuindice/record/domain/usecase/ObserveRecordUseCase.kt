package com.gdavidpb.tuindice.record.domain.usecase

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
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, ObservedRecord, Nothing>() {
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

			val currentTermId = currentSelectedTermId(
				viewMode = viewMode,
				selectedHistoricalTermId = selectedHistoricalTermId,
				selectedProjectionTermId = selectedProjectionTermId
			)
			val selectedTermId = resolveSelectedTermId(
				viewMode = viewMode,
				visibleTermIds = visibleTermIds,
				selectedHistoricalTermId = selectedHistoricalTermId,
				selectedProjectionTermId = selectedProjectionTermId
			)

			// Only self-heal (and persist) when this view mode never had an explicit
			// selection. A non-null currentTermId that isn't (yet) in visibleTermIds is
			// more likely a record snapshot that hasn't caught up than a genuine orphan:
			// this combine() also reacts to recordSelectionRepository's own writes, which
			// aren't Room-backed and dispatch immediately, while the confirmed record
			// snapshot is and can lag behind. Persisting the transient fallback here would
			// permanently clobber a fresher explicit selection (e.g. right after creating
			// a synthetic term). The resolved fallback above still keeps this emission's
			// own selectedTermId valid for display without needing to persist it.
			if ((selectedTermId != null) && (currentTermId == null)) {
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
