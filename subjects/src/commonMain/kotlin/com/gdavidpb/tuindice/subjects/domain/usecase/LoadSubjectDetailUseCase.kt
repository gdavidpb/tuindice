package com.gdavidpb.tuindice.subjects.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailLoad
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectStatsRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectDetailParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoadSubjectDetailUseCase(
	private val subjectStatsRepository: SubjectStatsRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<SubjectDetailParams, SubjectDetailLoad, Nothing>() {
	override suspend fun executeOnBackground(params: SubjectDetailParams): Flow<SubjectDetailLoad> {
		return flow {
			val localResult = subjectStatsRepository.getFreshSubjectDetail(params.subjectCode)

			if (localResult != null) {
				emit(SubjectDetailLoad.Data(localResult))
				return@flow
			}

			emit(SubjectDetailLoad.LoadingRemote)
			emit(
				SubjectDetailLoad.Data(
					subjectStatsRepository.refreshSubjectDetail(params.subjectCode)
				)
			)
		}
	}
}
