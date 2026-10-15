// Fixture de semgrep --test para data-source.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// Los paths (data/source/** y data/**) y la exención por archivo del seam AEAD no aplican en --test: cada
// coincidencia del patrón se anota aquí, y las exenciones se validan en la prueba de generalidad.
package com.gdavidpb.tuindice.sample.data.source

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

// ruleid: data-source-no-interface
interface SampleSeam {
	fun read(): String
}

// ruleid: data-source-no-interface
internal fun interface SampleFunSeam {
	fun read(): String
}

// ok: data-source-no-interface
class SampleConcreteDataSource {
	fun read(): String = "sample"
}

// ruleid: data-source-no-flow-wrapper
fun wrappedSample(source: Flow<String>): Flow<String> = flow { emitAll(source) }

// ruleid: data-source-no-flow-wrapper
fun wrappedTypedSample(source: Flow<String>): Flow<String> = flow<String> {
	emitAll(source)
}

// ok: data-source-no-flow-wrapper
fun stampedSample(source: Flow<String>): Flow<String> = flow {
	emit("start")
	emitAll(source)
}
