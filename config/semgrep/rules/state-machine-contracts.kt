// Fixture de semgrep --test para state-machine-contracts.yaml.
// La clase completa (con las dos llamadas) va primero; la clase incompleta va al final: la comprobación mira
// desde la declaración hasta el final del archivo, así que un archivo con una sola prueba (el convencional)
// es el caso real. Los paths (*StateMachineContractTest.kt) se validan en la prueba de generalidad.
package com.gdavidpb.tuindice.sample.presentation.machine

// ok: state-machine-contract-test-complete
class SampleCompleteStateMachineContractTest {
	fun machine_exportsDeclaredTransitionsToMermaid() {
		val diagram = machine.exportToMermaid(machineName = "sample")
		assertMachineRandomWalk(machine = machine, steps = 10)
	}
}

// ruleid: state-machine-contract-test-complete
class SampleMissingStateMachineContractTest {
	fun machine_declaresNothing() = Unit
}
