package com.gdavidpb.tuindice.subjects.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectDetailParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RefreshSubjectDetailUseCase(
	private val subjectStatsRepository: SubjectStatsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<SubjectDetailParams, SubjectDetailResult, Nothing>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: SubjectDetailParams): Flow<SubjectDetailResult> {
		return flowOf(
			subjectStatsRepository.refreshSubjectDetail(subjectCode = params.subjectCode)
		)
	}
}
