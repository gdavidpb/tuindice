package com.gdavidpb.tuindice.subjects.domain.usecase

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RefreshSubjectSearchUseCase(
	private val subjectCatalogRepository: SubjectCatalogRepository,
	override val reportingRepository: ReportingRepository,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<SubjectSearchParams, Unit, Nothing>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
	override suspend fun executeOnBackground(params: SubjectSearchParams): Flow<Unit> {
		subjectCatalogRepository.refreshSearchResults(
			query = params.query,
			limit = params.limit
		)
		return flowOf(Unit)
	}
}
