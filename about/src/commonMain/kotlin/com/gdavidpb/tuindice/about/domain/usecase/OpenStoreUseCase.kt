package com.gdavidpb.tuindice.about.domain.usecase

import com.gdavidpb.tuindice.about.data.repository.StoreUrlDataSource
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class OpenStoreUseCase(
	private val storeUrlDataSource: StoreUrlDataSource
) : FlowUseCase<Unit, String, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<String> {
		val url = storeUrlDataSource.getStoreUrl()

		return flowOf(url)
	}
}
