package com.gdavidpb.tuindice.sampleflow.data.source

import kotlinx.coroutines.flow.Flow

// Disparo de data-source-no-interface: la interfaz debería estar en data/repository.
interface SampleFlowSeam {
	fun read(): String
}

class SampleFlowStreamDataSource(
	private val source: Flow<String>
) {
	fun observe(): Flow<String> = source
}
