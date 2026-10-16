// Fixture de semgrep --test para data-source.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// Los paths (data/source/**) y la exención por archivo del seam AEAD no aplican en --test: cada
// coincidencia del patrón se anota aquí, y las exenciones se validan en la prueba de generalidad.
package com.gdavidpb.tuindice.sample.data.source

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
