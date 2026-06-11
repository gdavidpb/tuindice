package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.about.domain.repository.StoreUrlRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class OpenStoreUseCase(
	private val storeUrlRepository: StoreUrlRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		val url = storeUrlRepository.getStoreUrl()

		return flowOf(url)
	}
}
