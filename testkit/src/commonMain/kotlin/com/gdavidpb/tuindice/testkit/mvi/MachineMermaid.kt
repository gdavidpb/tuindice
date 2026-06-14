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
 * The diagram is flat (no composite wrapper): a `state X { … X --> … }` wrapper makes
 * Mermaid treat the X edges as children of X and rejects the self-parent cycle. Edges are
 * labelled `σ / λ` (event / emitted effects); machine-level (`fromAny`) rows render from a
 * single `"any state"` pseudo-node aliased to [machineName].
 *
 * A `%% machine: <name>` comment carries [machineName] (Mermaid ignores `%%` lines): with no
 * wrapper, machines without `fromAny` rows would otherwise not mention the name anywhere, and
 * dump-machine-diagrams.sh names each `.mmd` file from this anchor.
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

	val hasMachineLevel = table.any { spec -> spec.from == null }

	return buildString {
		appendLine("stateDiagram-v2")
		appendLine("%% machine: $machineName")
		states.forEach { state ->
			val id = state.stateId()
			val label = state.stateLabel()
			if (id == label) appendLine("state $id")
			else appendLine("state \"$label\" as $id")
		}
		if (hasMachineLevel) {
			appendLine("state \"any state\" as $machineName")
		}
		appendLine()
		appendLine("[*] --> ${initialState.stateId()}")
		table.forEach { spec ->
			val event = spec.on.simpleName
			val outputs = spec.emits.mapNotNull { effect -> effect.simpleName }
			val label = if (outputs.isEmpty()) event else "$event / ${outputs.joinToString(" · ")}"
			val from = spec.from?.stateId() ?: machineName
			val to = spec.to?.stateId() ?: from
			appendLine("$from --> $to : $label")
		}
	}.trimEnd()
}

// Mermaid reserves these as keywords; a bare node id that collides with one
// (e.g. a state class literally named `State` → `state`) is a parse error.
private val MERMAID_RESERVED_WORDS = setOf(
	"state", "as", "note", "end", "direction", "hide",
	"class", "classdef", "click", "style", "link", "call", "callback", "href"
)

private fun KClass<*>.stateLabel(): String {
	return simpleName?.toSnakeCase() ?: "unknown"
}

/**
 * The node id used in declarations and edges. Equal to [stateLabel] unless the label
 * collides with a Mermaid keyword, in which case it is suffixed into a safe id and the
 * readable name survives as the node's quoted label (`state "state" as state_node`).
 */
private fun KClass<*>.stateId(): String {
	val label = stateLabel()
	return if (label in MERMAID_RESERVED_WORDS) "${label}_node" else label
}
