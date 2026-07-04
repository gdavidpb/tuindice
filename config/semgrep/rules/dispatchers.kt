// Fixture de semgrep --test para dispatchers.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
package com.gdavidpb.tuindice.sample.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class SampleUseCase(
	// ruleid: pipeline-no-dispatcher-injection
	private val dispatchers: TuIndiceDispatchers
) {
	suspend fun run(): Int {
		// ruleid: no-raw-dispatchers, pipeline-no-context-switch
		return withContext(Dispatchers.IO) { 1 }
	}

	fun observe(flow: Flow<Int>): Flow<Int> =
		// ruleid: pipeline-no-context-switch
		flow.flowOn(dispatchers.default)

	fun observeRaw(flow: Flow<Int>): Flow<Int> =
		// ruleid: no-raw-dispatchers, pipeline-no-context-switch
		flow.flowOn(Dispatchers.Default)

	fun label(): String {
		// ruleid: no-raw-dispatchers
		val dispatcher = Dispatchers.Main
		return dispatcher.toString()
	}
}

class SampleInheritingUseCase {
	// ok: pipeline-no-dispatcher-injection
	suspend fun run(block: suspend () -> Int): Int {
		// ok: pipeline-no-context-switch
		return block()
	}
}
