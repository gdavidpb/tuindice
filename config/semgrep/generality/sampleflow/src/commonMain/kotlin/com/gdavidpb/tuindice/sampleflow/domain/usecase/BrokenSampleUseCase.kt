package com.gdavidpb.tuindice.sampleflow.domain.usecase

import com.gdavidpb.tuindice.sampleflow.data.model.SampleFlowEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class BrokenSampleUseCase(
	private val dispatchers: TuIndiceDispatchers,
	private val delegate: BrokenSampleUseCase?
) {
	suspend fun run(entity: SampleFlowEntity): Any {
		try {
			return delegate?.executeOnBackground(entity) ?: UseCaseState.Data(entity)
		} catch (exception: Exception) {
			return UseCaseState.Error(null)
		}
	}

	fun observe(flow: Flow<Int>): Flow<Int> = flow.flowOn(Dispatchers.Default)
}
