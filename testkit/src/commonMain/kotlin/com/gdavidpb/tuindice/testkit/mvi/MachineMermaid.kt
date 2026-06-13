package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.utils.extension.toSnakeCase
import kotlin.reflect.KClass

/**
 * Renders the machine's transition table as a Mermaid state diagram. Test-only tooling:
 * the engine in `base` keeps only `machine.table` as its introspectable surface, and the
 * rendering — consumed solely by contract tests that publish the diagrams as docs
 * artifacts — lives here so it never ships in the production artifact.
 *
 * Edges are labelled `σ / λ` (event / emitted effects); machine-level (`fromAny`) rows
 * render as self-loops on the [machineName] node.
 */
fun <S : ViewState> MachineDefinition<S>.exportToMermaid(
	machineName: String,
	initialState: KClass<out S>
): String {
	val states = buildList {
		add(initialState)
		table.forEach { spec ->
			spec.from?.let(::add)
			spec.to?.let(::add)
		}
	}.distinct()

	return buildString {
		appendLine("stateDiagram-v2")
		appendLine("state $machineName {")
		states.forEach { state ->
			appendLine("    state ${state.stateName()}")
		}
		appendLine()
		appendLine("    [*] --> ${initialState.stateName()}")
		table.forEach { spec ->
			val event = spec.on.simpleName
			val outputs = spec.emits.mapNotNull { effect -> effect.simpleName }
			val label = if (outputs.isEmpty()) event else "$event / ${outputs.joinToString(" · ")}"
			val from = spec.from?.stateName() ?: machineName
			val to = spec.to?.stateName() ?: from
			appendLine("    $from --> $to : $label")
		}
		append("}")
	}
}

private fun KClass<*>.stateName(): String {
	return simpleName?.toSnakeCase() ?: "unknown"
}
