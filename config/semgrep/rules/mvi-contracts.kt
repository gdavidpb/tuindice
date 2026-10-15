// Fixture de semgrep --test para mvi-contracts.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// La exención de contract/ y el ViewState de chrome de ruta se validan en la
// prueba de generalidad (en --test los paths no aplican).
package com.gdavidpb.tuindice.sample.presentation.route

// ruleid: mvi-actions-effects-in-contract
object RogueAction : ViewAction

// ruleid: mvi-actions-effects-in-contract, presentation-no-toplevel-data-class
data class RogueEffect(val url: String) : ViewEffect

// ok: mvi-actions-effects-in-contract
class RouteChromeState(val title: String) : ViewState

// ruleid: presentation-no-toplevel-data-class
data class RogueNotice(val text: String)

// ruleid: presentation-no-toplevel-data-class
private data class RoguePrivateNotice(val text: String)

class SampleHostMachine(
	// ruleid: machine-no-host-property
	private val host: Any
) {
	// ruleid: machine-no-host-property
	var host: Any? = null

	// ok: machine-no-host-property
	fun define(host: Any): Any = host
}

class SampleNoticeHolder {
	// ok: presentation-no-toplevel-data-class
	data class SampleNestedNotice(val text: String)
}
