// Fixture de semgrep --test para naming.yaml. La regla reporta una vez por archivo: en el primer tipo público
// de un par. Por eso el ruleid va sobre el primer tipo público y el resto del archivo solo tiene tipos
// privados y subtipos anidados (que no cuentan). Un archivo con dos tipos públicos fuera de este fixture es el caso
// que la regla marca.
package com.gdavidpb.tuindice.sample.domain.model

// ruleid: naming-one-public-type-per-file
class SampleFirst

class SampleSecond

// ok: naming-one-public-type-per-file
private class SamplePrivateHelper

// ok: naming-one-public-type-per-file
private sealed interface SamplePrivateState {
	data object Idle : SamplePrivateState

	data class Busy(val since: Long) : SamplePrivateState
}
