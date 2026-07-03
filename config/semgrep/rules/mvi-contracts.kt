// Fixture de semgrep --test para mvi-contracts.yaml.
// No es código del proyecto: cada línea anotada valida una regla.
// La exención de contract/ y el ViewState de chrome de ruta se validan en la
// prueba de generalidad (en --test los paths no aplican).
package com.gdavidpb.tuindice.sample.presentation.route

// ruleid: mvi-actions-effects-in-contract
object RogueAction : ViewAction

// ruleid: mvi-actions-effects-in-contract
data class RogueEffect(val url: String) : ViewEffect

// ok: mvi-actions-effects-in-contract
data class RouteChromeState(val title: String) : ViewState

class SampleHostMachine(
	// ruleid: machine-no-host-property
	private val host: Any
) {
	// ruleid: machine-no-host-property
	var host: Any? = null

	// ok: machine-no-host-property
	fun define(host: Any): Any = host
}
