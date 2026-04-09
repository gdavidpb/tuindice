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
) : FlowUseCase<Unit, ObservedRecord, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: Unit): Flow<ObservedRecord> {
		return combine(
			academicRecordRepository.observeAcademicRecordFlow(),
			recordSelectionRepository.observeRecordViewMode(),
			recordSelectionRepository.observeSelectedTermId(RecordViewMode.Official),
			recordSelectionRepository.observeSelectedTermId(RecordViewMode.Working)
		) { record, viewMode, selectedOfficialTermId, selectedWorkingTermId ->
			val visibleTermIds = record.filteredProjectionFor(viewMode)
				.terms
				.map { term -> term.id }

			val selectedTermId = resolveSelectedTermId(
				viewMode = viewMode,
				visibleTermIds = visibleTermIds,
				selectedOfficialTermId = selectedOfficialTermId,
				selectedWorkingTermId = selectedWorkingTermId
			)

			if ((selectedTermId != null) && (selectedTermId != currentSelectedTermId(
					viewMode = viewMode,
					selectedOfficialTermId = selectedOfficialTermId,
					selectedWorkingTermId = selectedWorkingTermId
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
				selectedTermId = selectedTermId
			)
		}
	}

	private fun resolveSelectedTermId(
		viewMode: RecordViewMode,
		visibleTermIds: List<String>,
		selectedOfficialTermId: String?,
		selectedWorkingTermId: String?
	): String? {
		if (visibleTermIds.isEmpty()) return null

		val currentSelected = currentSelectedTermId(
			viewMode = viewMode,
			selectedOfficialTermId = selectedOfficialTermId,
			selectedWorkingTermId = selectedWorkingTermId
		)
		val mirroredSelected = currentSelectedTermId(
			viewMode = viewMode.other(),
			selectedOfficialTermId = selectedOfficialTermId,
			selectedWorkingTermId = selectedWorkingTermId
		)

		return listOfNotNull(currentSelected, mirroredSelected)
			.firstOrNull { candidate -> candidate in visibleTermIds }
			?: visibleTermIds.first()
	}

	private fun currentSelectedTermId(
		viewMode: RecordViewMode,
		selectedOfficialTermId: String?,
		selectedWorkingTermId: String?
	): String? {
		return when (viewMode) {
			RecordViewMode.Official -> selectedOfficialTermId
			RecordViewMode.Working -> selectedWorkingTermId
		}
	}
}
