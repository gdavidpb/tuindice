package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.about.domain.repository.AboutRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LoadVersionUseCase(
	private val aboutRepository: AboutRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		val versionDescription = aboutRepository.getVersionDescription()

		return flowOf(versionDescription)
	}
}
