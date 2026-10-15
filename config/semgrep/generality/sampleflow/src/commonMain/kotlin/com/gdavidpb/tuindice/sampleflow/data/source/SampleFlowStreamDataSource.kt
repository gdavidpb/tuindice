package com.gdavidpb.tuindice.sampleflow.data.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// Disparos de data-source-no-interface (la interfaz debería estar en data/repository) y de
// data-source-no-flow-wrapper (el flujo solo reenvía la fuente).
interface SampleFlowSeam {
	fun read(): String
}

class SampleFlowStreamDataSource(
	private val source: Flow<String>
) {
	fun observe(): Flow<String> = flow {
		emitAll(source)
	}
}
