package com.gdavidpb.tuindice.subjects.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSearchResult
import com.gdavidpb.tuindice.subjects.domain.repository.SubjectCatalogRepository
import com.gdavidpb.tuindice.subjects.domain.usecase.param.SubjectSearchParams
import kotlinx.coroutines.flow.Flow

class ObserveSubjectSearchUseCase(
	private val subjectCatalogRepository: SubjectCatalogRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<SubjectSearchParams, List<SubjectSearchResult>, Nothing>() {
	override suspend fun executeOnBackground(params: SubjectSearchParams): Flow<List<SubjectSearchResult>> {
		return subjectCatalogRepository.observeSearchResults(
			query = params.query,
			limit = params.limit
		)
	}
}
